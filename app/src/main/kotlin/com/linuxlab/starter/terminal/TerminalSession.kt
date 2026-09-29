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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.linuxlab.starter.terminal.emulator.TerminalBuffer
import com.linuxlab.starter.terminal.pty.PtyNative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * 真实终端会话：proot + Alpine rootfs + 原生 PTY。
 * 会话状态用 Compose state 暴露，UI 直接观察。
 */
class TerminalSession(private val context: Context) {

    enum class State { IDLE, INSTALLING, RUNNING, EXITED, FAILED }

    val buffer = TerminalBuffer(80, 24)

    var state: State by mutableStateOf(State.IDLE)
        private set
    var message: String? by mutableStateOf(null)
        private set
    var progress: Float by mutableStateOf(0f)
        private set

    /** 启动自检日志：正常时静默，只在失败界面展示 */
    var startLog: List<String> by mutableStateOf(emptyList())
        private set

    /** 兜底模式：设备禁止执行数据目录中的二进制，仅 shell 可用（无 apk） */
    var degraded: Boolean by mutableStateOf(false)
        private set

    private var fd = -1
    private var job: Job? = null

    private val decoder = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)

    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.IO) {
            runCatching {
                state = State.INSTALLING
                val install = Bootstrap.ensureInstalled(context.applicationContext) { pct, msg ->
                    progress = pct / 100f
                    message = msg
                }
                if (!install.ok) {
                    message = install.message
                    state = State.FAILED
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    startLog = install.preflight
                    degraded = install.shellBind != null
                }

                val proot = install.proot!!
                val rootfs = install.rootfs!!

                File(context.filesDir, "home").mkdirs()
                Bootstrap.prootTmpDir(context) // 必须提前建好，否则 proot 建 glue 会失败

                val command = Bootstrap.buildCommand(context, rootfs, proot, install.shellBind)
                val env = Bootstrap.buildEnvironment(context)
                val fd = PtyNative.createPty(command, env, "/", buffer.rows, buffer.columns)
                if (fd < 0) {
                    message = "创建 PTY 失败（errno 见 logcat）。可能是系统限制了 fork/exec。"
                    state = State.FAILED
                    return@launch
                }
                this@TerminalSession.fd = fd
                withContext(Dispatchers.Main) {
                    state = State.RUNNING
                    message = null
                }
                readerLoop(fd)
            }.onFailure { e ->
                withContext(Dispatchers.Main) {
                    message = e.message ?: "启动失败"
                    state = State.FAILED
                }
            }
        }
    }

    private suspend fun readerLoop(fd: Int) {
        val buf = ByteArray(16384)
        while (fd >= 0) {
            val n = PtyNative.readPty(fd, buf)
            when {
                n > 0 -> {
                    val text = decoder.decode(ByteBuffer.wrap(buf, 0, n)).toString()
                    withContext(Dispatchers.Main) { buffer.feed(text) }
                }
                n == 0 -> break
                n == -2 -> delay(16)
                else -> break
            }
        }
        withContext(Dispatchers.Main) {
            if (state == State.RUNNING) {
                buffer.feed("\r\n[会话已结束 — 点右上角 ⟳ 重新开始]\r\n")
                state = State.EXITED
            }
        }
    }

    fun write(bytes: ByteArray) {
        if (fd < 0) return
        runCatching { PtyNative.writePty(fd, bytes, bytes.size) }
    }

    fun write(text: String) = write(text.toByteArray(StandardCharsets.UTF_8))

    fun resize(rows: Int, columns: Int) {
        buffer.resize(columns, rows)
        if (fd >= 0) runCatching { PtyNative.resizePty(fd, rows, columns) }
    }

    fun restart(scope: CoroutineScope) {
        stop()
        buffer.clearScrollback()
        state = State.IDLE
        message = null
        progress = 0f
        start(scope)
    }

    /** 跑一组诊断命令，方便定位问题 */
    fun runDiagnostics() {
        write("clear; echo '--- 诊断 ---'; uname -a; head -2 /etc/os-release; echo; id; echo; ls /; echo; echo \$PATH; echo; ls -l /bin/sh /bin/busybox /bin/busybox.static 2>&1; echo; ls -l /lib/ld-musl-aarch64.so.1 2>&1; echo; mount 2>/dev/null | grep -E ' /data | noexec' ; echo '--- 结束 ---'\r")
    }

    fun stop() {
        job?.cancel()
        job = null
        if (fd >= 0) {
            runCatching { PtyNative.closePty(fd) }
            fd = -1
        }
    }
}
