/*
 * Linux 入门 —— Linux 命令学习与真实终端 App
 * Copyright (C) 2026 拾星*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.linuxlab.starter.terminal

import android.content.Context
import android.os.Build
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * 首次启动时把内置的 Alpine Linux rootfs 解压到 App 私有目录，并准备 proot。
 * 全程离线，不联网。
 */
object Bootstrap {

    const val ROOTFS_VERSION = "alpine-3.20.3-aarch64-tar-v2"

    data class InstallResult(
        val ok: Boolean,
        val rootfs: File? = null,
        val proot: File? = null,
        val message: String = "",
        val preflight: List<String> = emptyList(),
        /** 当数据目录里的二进制无法 exec 时，改用这个 /data/app 下的二进制 bind 进 rootfs */
        val shellBind: File? = null
    )

    /** 系统从 APK 解压出来的原生库（/data/app/.../lib/arm64），这类文件允许执行 */
    fun nativeLib(context: Context, name: String): File =
        File(context.applicationInfo.nativeLibraryDir, name)

    /** 找到可执行的 proot（来自 jniLibs，随 APK 安装并解压） */
    fun findProot(context: Context): File? {
        val candidates = listOf(
            File(context.applicationInfo.nativeLibraryDir, "libproot.so"),
            File(context.filesDir, "proot"),
            File(context.applicationInfo.dataDir, "lib/libproot.so")
        )
        return candidates.firstOrNull { it.exists() && it.length() > 100_000 && it.canExecute() }
    }

    fun isInstalled(context: Context): Boolean {
        val stamp = File(context.filesDir, "rootfs/.installed")
        val rootfs = File(context.filesDir, "rootfs")
        return stamp.exists() && File(rootfs, "bin/sh").exists() && findProot(context) != null
    }

    fun rootfsDir(context: Context) = File(context.filesDir, "rootfs")

    /** proot 的 glue rootfs 需要一个宿主机上的可写临时目录（Android 没有 /tmp） */
    fun prootTmpDir(context: Context): File {
        val dir = File(context.filesDir, "ptmp")
        if (!dir.exists()) dir.mkdirs()
        runCatching { dir.setExecutable(true, false); dir.setWritable(true, false) }
        return dir
    }

    fun ensureInstalled(
        context: Context,
        onProgress: (Int, String) -> Unit
    ): InstallResult {
        val proot = findProot(context)
            ?: return InstallResult(
                false,
                message = "未找到 proot（当前 ABI：${Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}，本包内置 arm64-v8a）"
            )

        val rootfs = rootfsDir(context)
        val stamp = File(rootfs, ".installed")
        val reinstall = !(stamp.exists() && stamp.readText().trim() == ROOTFS_VERSION && File(rootfs, "bin/sh").exists())

        if (reinstall) {
            try {
                onProgress(2, "正在解压 Linux 系统…")
                if (rootfs.exists()) rootfs.deleteRecursively()
                rootfs.mkdirs()

                // 注意：AGP 会把 assets 里的 .gz 自动解压后再打包，
                // 因此内置的是未压缩的 tar（APK 的 zip 层会负责压缩）。
                context.assets.open("alpine-aarch64.tar").use { stream ->
                    TarArchiveInputStream(stream).use { tar -> extract(tar, rootfs, onProgress) }
                }
                onProgress(92, "修复文件权限…")
                fixPermissions(rootfs)
                onProgress(96, "写入系统配置…")
                prepareRuntime(context, rootfs)
                stamp.writeText(ROOTFS_VERSION)
            } catch (e: Exception) {
                return InstallResult(false, message = "解压失败：${e.message}")
            }
        } else {
            // 已安装也要确保临时目录与静态 shell 就位
            runCatching { fixPermissions(rootfs) }
            runCatching { prepareRuntime(context, rootfs) }
        }

        val (preflight, bind) = runPreflight(context, rootfs)
        onProgress(100, "完成")
        return InstallResult(true, rootfs, proot, "安装完成", preflight, bind)
    }

    private fun extract(
        tar: TarArchiveInputStream,
        target: File,
        onProgress: (Int, String) -> Unit
    ) {
        var entry: TarArchiveEntry? = tar.nextEntry
        var count = 0
        while (entry != null) {
            val out = File(target, entry.name)
            val path = out.canonicalPath
            if (!path.startsWith(target.canonicalPath)) {
                entry = tar.nextEntry
                continue
            }
            when {
                entry.isDirectory -> out.mkdirs()
                entry.isSymbolicLink -> {
                    out.parentFile?.mkdirs()
                    runCatching {
                        if (out.exists() || Files.isSymbolicLink(out.toPath())) out.delete()
                        Files.createSymbolicLink(out.toPath(), Paths.get(entry.linkName))
                    }
                }
                entry.isLink -> { // 硬链接退化为拷贝
                    val link = File(target, entry.linkName)
                    out.parentFile?.mkdirs()
                    runCatching {
                        if (link.exists()) link.copyTo(out, overwrite = true)
                        else copyStream(tar, out)
                    }
                }
                else -> {
                    out.parentFile?.mkdirs()
                    copyStream(tar, out)
                }
            }
            count++
            if (count % 40 == 0) {
                onProgress((count * 90 / 518).coerceIn(0, 90), "正在解压 Linux 系统… ($count/518)")
            }
            entry = tar.nextEntry
        }
    }

    /**
     * 统一修权限：不依赖 tar 里记录的 mode，按路径策略兜底。
     * Android 上一旦 busybox / ld-musl 少了 +x，proot 就会报 execve Permission denied。
     */
    private fun fixPermissions(root: File) {
        val execDirs = listOf("/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/", "/usr/local/bin/", "/usr/local/sbin/", "/usr/libexec/")
        root.walkTopDown().forEach { file ->
            if (Files.isSymbolicLink(file.toPath())) return@forEach
            if (file.isDirectory) {
                file.setReadable(true, false)
                file.setExecutable(true, false)
            } else {
                val path = file.absolutePath
                val shouldExec = execDirs.any { path.contains(it) } ||
                    file.name.startsWith("ld-musl") ||
                    (file.name.contains(".so") && path.contains("/lib/")) ||
                    file.name == "busybox" || file.name == "busybox.static"
                file.setReadable(true, false)
                file.setExecutable(shouldExec, false)
            }
        }
    }

    private fun copyStream(tar: TarArchiveInputStream, out: File) {
        FileOutputStream(out).use { fos ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = tar.read(buf)
                if (n <= 0) break
                fos.write(buf, 0, n)
            }
        }
    }

    /** 网络、临时目录、静态 shell 等运行时配置 */
    private fun prepareRuntime(context: Context, rootfs: File) {
        runCatching {
            File(rootfs, "etc/resolv.conf").writeText("nameserver 8.8.8.8\nnameserver 1.1.1.1\n")
        }
        runCatching {
            File(rootfs, "etc/hosts").writeText("127.0.0.1 localhost\n::1 localhost\n")
        }
        listOf("tmp", "proc", "sys", "dev", "sdcard").forEach {
            runCatching { File(rootfs, it).mkdirs() }
        }
        runCatching { File(rootfs, "tmp").apply { mkdirs(); setExecutable(true, false); setWritable(true, false) } }

        // 静态 busybox：不依赖动态解释器，即使 ld-musl 出问题 shell 也能起来
        runCatching {
            val target = File(rootfs, "bin/busybox.static")
            if (!target.exists()) {
                context.assets.open("busybox-static-aarch64").use { input ->
                    FileOutputStream(target).use { output -> input.copyTo(output) }
                }
            }
            target.setExecutable(true, false)
            target.setReadable(true, false)
        }

        val profile = File(rootfs, "root/.profile")
        if (!profile.exists()) {
            runCatching {
                profile.parentFile?.mkdirs()
                profile.writeText(
                    """
                    export PS1='\u@\h:\w\$ '
                    export PAGER=less
                    alias ll='ls -alFh'
                    alias la='ls -A'
                    """.trimIndent()
                )
            }
        }
    }

    /** 直接执行探测：null 表示成功，否则返回错误说明 */
    private fun execProbe(binary: File): String? {
        if (!binary.exists()) return "文件不存在"
        if (!binary.canExecute()) return "无执行权限(canExecute=false)"
        return try {
            val process = Runtime.getRuntime().exec(arrayOf(binary.absolutePath, "true"))
            val finished = process.waitFor(15, TimeUnit.SECONDS)
            val code = if (finished) process.exitValue() else -999
            runCatching { process.destroy() }
            when {
                !finished -> "执行超时"
                code == 0 -> null
                else -> "退出码 $code"
            }
        } catch (e: Exception) {
            e.message ?: e.javaClass.simpleName
        }
    }

    /**
     * 启动前自检：确认 App 私有目录里的二进制能否被 exec，
     * 并决定 /bin/sh 指向静态还是动态 busybox。
     */
    private fun runPreflight(context: Context, rootfs: File): Pair<List<String>, File?> {
        val lines = mutableListOf<String>()
        val staticBin = File(rootfs, "bin/busybox.static")
        val dynBin = File(rootfs, "bin/busybox")

        val staticErr = execProbe(staticBin)
        val dynErr = execProbe(dynBin)

        // 优先静态：不依赖 ld-musl
        val prefer = when {
            staticErr == null -> staticBin
            dynErr == null -> dynBin
            else -> null
        }

        // 兜底：数据目录整体被禁止 exec（Android 10+ / targetSdk>=29 / 部分 ROM）时，
        // 用随 APK 安装的 /data/app/.../lib/arm64/libbusybox.so（系统解压，允许执行）
        var bind: File? = null
        if (prefer == null) {
            val libBusybox = nativeLib(context, "libbusybox.so")
            val libErr = execProbe(libBusybox)
            if (libErr == null) bind = libBusybox
            lines += "[自检] /data/app 内置 busybox : ${libErr ?: "可执行 ✓（用作兜底）"}"
        }

        if (prefer != null || bind != null) {
            runCatching {
                val sh = File(rootfs, "bin/sh")
                if (sh.exists() || Files.isSymbolicLink(sh.toPath())) sh.delete()
                // 用 bind 时系统里的 /bin/busybox 会被替换成 /data/app 里的那份
                Files.createSymbolicLink(sh.toPath(), Paths.get("busybox"))
            }
        }

        lines += "[自检] 静态 busybox : ${staticErr ?: "可执行 ✓"}"
        lines += "[自检] 动态 busybox : ${dynErr ?: "可执行 ✓"}"
        lines += "[自检] /bin/sh → ${prefer?.name ?: if (bind != null) "busybox（bind 自 /data/app）" else "（不可执行！）"}"
        lines += "[自检] proot 路径 : ${findProot(context)?.absolutePath ?: "未找到"}"
        lines += "[自检] 临时目录 : ${prootTmpDir(context).absolutePath}"
        if (bind != null) {
            lines += "[提示] 本设备禁止执行 App 数据目录中的二进制，已改用 APK 内置二进制启动 shell；"
            lines += "[提示] 卸载旧版本后重启手机再安装，可解除该限制并获得完整环境（含 apk 包管理器）。"
        }
        lines += ""
        return lines to bind
    }

    /** 组装 proot 命令行 */
    fun buildCommand(context: Context, rootfs: File, proot: File, shellBind: File? = null): Array<String> {
        val external = context.getExternalFilesDir(null)?.absolutePath
        val args = mutableListOf(
            proot.absolutePath,
            "--rootfs=${rootfs.absolutePath}",
            "--cwd=/root",
            "--root-id",
            "--kill-on-exit",
            "--link2symlink",
            "--bind=/proc",
            "--bind=/dev",
            "--bind=/sys",
            "--bind=/system",
            "--bind=${context.filesDir.absolutePath}/home:/home"
        )
        if (external != null) {
            File(external).mkdirs()
            args += "--bind=$external:/sdcard"
        }
        // 数据目录不可执行时，把 /data/app 里的静态 busybox 挂到 /bin/busybox
        if (shellBind != null && shellBind.canExecute()) {
            args += "--bind=${shellBind.absolutePath}:/bin/busybox"
        }
        args += listOf("/bin/sh", "-l")
        return args.toTypedArray()
    }

    fun buildEnvironment(context: Context): Array<String> {
        val external = context.getExternalFilesDir(null)?.absolutePath
        val tmp = prootTmpDir(context)
        val env = mutableListOf(
            "PROOT_TMP_DIR=${tmp.absolutePath}",
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "HOME=/root",
            "TERM=xterm-256color",
            "COLORTERM=truecolor",
            "LANG=C.UTF-8",
            "SHELL=/bin/sh",
            "PAGER=less",
            "TMPDIR=/tmp",
            "ANDROID_DATA=/data",
            "ANDROID_ROOT=/system"
        )
        if (external != null) env += "EXTERNAL_STORAGE=$external"
        return env.toTypedArray()
    }
}
