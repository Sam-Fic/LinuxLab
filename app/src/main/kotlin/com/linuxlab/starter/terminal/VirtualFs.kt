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

/**
 * 一个内存里的迷你 Linux 文件系统，供模拟终端使用。
 * 目录的 content 为 null，文件的 content 为字符串。
 */
class FsNode(
    var name: String,
    var parent: FsNode? = null,
    var content: String? = null
) {
    val children = LinkedHashMap<String, FsNode>()
    var mode = "rw-r--r--"

    val isDir: Boolean get() = content == null

    fun path(): String {
        if (parent == null) return "/"
        val parts = mutableListOf<String>()
        var n: FsNode? = this
        while (n != null && n.parent != null) {
            parts += n.name
            n = n.parent
        }
        return "/" + parts.asReversed().joinToString("/")
    }
}

class VirtualFs {

    val user = "learner"
    val host = "linux-lab"
    val homePath = "/home/$user"

    var root: FsNode = FsNode("/")
        private set
    var cwd: FsNode = root
        private set

    init { reset() }

    /** 重置成初始的练习环境 */
    fun reset() {
        root = FsNode("/")
        mkdirp("/home/$user/notes")
        mkdirp("/home/$user/projects")
        mkdirp("/etc")
        mkdirp("/var/log")
        mkdirp("/tmp")

        write("/home/$user/README.md", """
            欢迎来到 Linux 模拟终端！

            这是一个安全的沙盒：你在这里做的任何操作都不会影响真实系统。
            常用起步命令：
              pwd            我现在在哪？
              ls -l          看看这里有什么
              cat notes/todo.txt   查看文件内容
              help           查看本终端支持的所有命令
              tasks          查看实战任务清单
        """.trimIndent())

        write("/home/$user/notes/todo.txt", """
            买牛奶
            写周报
            学习 chmod
            备份照片
            学习 chmod
            交电费
        """.trimIndent())

        write("/home/$user/notes/quotes.txt", """
            Talk is cheap. Show me the code.
            Simplicity is the ultimate sophistication.
            Stay hungry, stay foolish.
        """.trimIndent())

        write("/home/$user/projects/hello.sh", """
            #!/bin/bash
            echo "Hello, Linux!"
            echo "Today is ${'$'}(date +%F)"
        """.trimIndent())

        write("/etc/hosts", """
            127.0.0.1   localhost
            127.0.1.1   linux-lab
            ::1         localhost ip6-localhost
        """.trimIndent())

        write("/etc/passwd", """
            root:x:0:0:root:/root:/bin/bash
            learner:x:1000:1000:Learner:/home/learner:/bin/bash
            www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin
        """.trimIndent())

        write("/var/log/app.log", """
            2024-09-01 09:00:01 INFO  server started on port 8080
            2024-09-01 09:05:12 INFO  user learner logged in
            2024-09-01 09:12:40 WARN  memory usage above 80%
            2024-09-01 09:20:03 ERROR database connection timeout
            2024-09-01 09:20:05 INFO  retrying connection
            2024-09-01 09:20:31 ERROR database connection timeout
            2024-09-01 09:21:02 INFO  connection recovered
            2024-09-01 10:00:00 INFO  daily job finished
        """.trimIndent())

        cwd = node(homePath) ?: root
    }

    // ---------- 路径工具 ----------

    fun expand(path: String): String = when {
        path == "~" -> homePath
        path.startsWith("~/") -> homePath + path.drop(1)
        else -> path
    }

    /** 解析路径（支持 . .. ~ 相对与绝对），不存在返回 null */
    fun node(path: String): FsNode? {
        val p = expand(path)
        var cur: FsNode? = if (p.startsWith("/")) root else cwd
        for (seg in p.split("/")) {
            if (seg.isEmpty() || seg == ".") continue
            if (seg == "..") {
                cur = cur?.parent ?: root
                continue
            }
            cur = cur?.children?.get(seg) ?: return null
        }
        return cur
    }

    fun exists(path: String): Boolean = node(path) != null

    /** 提示符里显示的路径，家目录用 ~ 表示 */
    fun promptPath(): String {
        val p = cwd.path()
        return if (p == homePath) "~" else if (p.startsWith("$homePath/")) "~" + p.removePrefix(homePath) else p
    }

    fun chdir(path: String): Boolean {
        if (path.isEmpty()) {
            cwd = node(homePath) ?: root
            return true
        }
        val target = node(path) ?: return false
        if (!target.isDir) return false
        cwd = target
        return true
    }

    /** 目标路径的父目录：相对且不含 / 时视为当前目录 */
    fun parentOf(path: String): FsNode? {
        val p = expand(path)
        val idx = p.lastIndexOf('/')
        return when {
            idx < 0 -> cwd
            idx == 0 -> root
            else -> node(p.substring(0, idx))
        }
    }

    fun nameOf(path: String): String = expand(path).substringAfterLast('/')

    /** 递归创建目录 */
    fun mkdirp(path: String): FsNode? {
        val p = expand(path)
        var cur = root
        for (seg in p.split("/")) {
            if (seg.isEmpty() || seg == ".") continue
            if (seg == "..") {
                cur = cur.parent ?: root
                continue
            }
            val next = cur.children[seg]
            cur = if (next == null) {
                val created = FsNode(seg, cur, null)
                cur.children[seg] = created
                created
            } else {
                if (!next.isDir) return null
                next
            }
        }
        return cur
    }

    /** 创建（或覆盖）文件；父目录必须存在 */
    fun write(path: String, content: String): Boolean {
        val parent = parentOf(path) ?: return false
        if (!parent.isDir) return false
        val name = nameOf(path)
        if (name.isEmpty() || name == "." || name == "..") return false
        val existing = parent.children[name]
        if (existing != null && existing.isDir) return false
        parent.children[name] = FsNode(name, parent, content)
        return true
    }

    /** 删除文件或（递归）目录 */
    fun remove(path: String, recursive: Boolean): Boolean {
        val target = node(path) ?: return false
        if (target === root) return false
        if (target.isDir && !recursive) return false
        if (target.isDir && target.children.isNotEmpty() && !recursive) return false
        target.parent?.children?.remove(target.name) ?: return false
        return true
    }

    fun copy(src: FsNode, dstPath: String, recursive: Boolean): Boolean {
        if (src.isDir && !recursive) return false
        val name = nameOf(dstPath)
        val parent = parentOf(dstPath) ?: return false
        if (!parent.isDir) return false

        fun clone(n: FsNode, p: FsNode): FsNode {
            val copy = FsNode(n.name, p, n.content)
            copy.mode = n.mode
            n.children.values.forEach { child -> copy.children[child.name] = clone(child, copy) }
            return copy
        }
        val targetExists = parent.children[name]
        val finalParent: FsNode = if (targetExists != null && targetExists.isDir) targetExists else parent
        val finalName = if (targetExists != null && targetExists.isDir) src.name else name
        if (finalName.isEmpty()) return false
        val copyNode = clone(src, finalParent)
        copyNode.name = finalName
        finalParent.children[finalName] = copyNode
        return true
    }

    fun move(srcPath: String, dstPath: String): Boolean {
        val src = node(srcPath) ?: return false
        if (src === root) return false
        val name = nameOf(dstPath)
        val parent = parentOf(dstPath) ?: return false
        if (!parent.isDir) return false
        val target = parent.children[name]
        val finalParent = if (target != null && target.isDir) target else parent
        val finalName = if (target != null && target.isDir) src.name else name
        if (finalName.isEmpty()) return false
        src.parent?.children?.remove(src.name)
        src.name = finalName
        src.parent = finalParent
        finalParent.children[finalName] = src
        return true
    }

    /** 目录内容（目录在前，其次按名称排序） */
    fun childrenOf(dir: FsNode): List<FsNode> =
        dir.children.values.sortedWith(compareBy({ !it.isDir }, { it.name }))

    /** 递归收集文件（供 find / tree 使用） */
    fun walk(dir: FsNode, includeDirs: Boolean = true, out: MutableList<FsNode> = mutableListOf()): List<FsNode> {
        dir.children.values.sortedBy { it.name }.forEach { child ->
            if (child.isDir) {
                if (includeDirs) out += child
                walk(child, includeDirs, out)
            } else {
                out += child
            }
        }
        return out
    }
}
