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

import com.linuxlab.starter.data.Repository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TermKind { COMMAND, OUTPUT, ERROR, SYSTEM, SUCCESS }
enum class TermSignal { NONE, CLEAR }

data class TermLine(val text: String, val kind: TermKind)
data class RunResult(val lines: List<TermLine>, val signal: TermSignal = TermSignal.NONE)

/** 实战任务：达标条件基于虚拟文件系统状态与已执行命令 */
data class PracticeTask(
    val title: String,
    val hint: String,
    val example: String,
    val done: (TerminalEngine) -> Boolean
)

object PracticeTasks {
    val all = listOf(
        PracticeTask(
            "① 新建一个目录",
            "用 mkdir 在家目录创建 practice 目录",
            "mkdir practice"
        ) { it.fs.exists("~/practice") },
        PracticeTask(
            "② 新建一个文件",
            "用 touch 创建空文件 hello.txt",
            "touch hello.txt"
        ) { it.fs.exists("~/hello.txt") },
        PracticeTask(
            "③ 用重定向写内容",
            "把一句话写进 greeting.txt（> 覆盖、>> 追加）",
            "echo \"hello linux\" > greeting.txt"
        ) { (it.fs.node("~/greeting.txt")?.content ?: "").contains("hello linux") },
        PracticeTask(
            "④ 复制文件",
            "把 notes/todo.txt 复制到家目录，命名为 todo.bak",
            "cp notes/todo.txt todo.bak"
        ) { it.fs.exists("~/todo.bak") },
        PracticeTask(
            "⑤ 重命名 / 移动",
            "用 mv 把 todo.bak 改名为 todo.copy",
            "mv todo.bak todo.copy"
        ) { it.fs.exists("~/todo.copy") },
        PracticeTask(
            "⑥ 用 grep 查日志",
            "在 /var/log/app.log 里找出所有 ERROR 行",
            "grep ERROR /var/log/app.log"
        ) { it.history.any { h -> h.startsWith("grep") } },
        PracticeTask(
            "⑦ 用管道组合命令",
            "把 grep 的结果交给 wc -l 统计行数",
            "grep ERROR /var/log/app.log | wc -l"
        ) { it.history.any { h -> h.contains("|") } }
    )

    fun finished(engine: TerminalEngine): Int = all.count { it.done(engine) }
}

/** 模拟终端引擎：解析命令行、支持管道与重定向、操作虚拟文件系统 */
class TerminalEngine {

    val fs = VirtualFs()
    val history = mutableListOf<String>()
    private var cursor = 0

    fun prompt(): String = "${fs.user}@${fs.host}:${fs.promptPath()}\$ "

    fun welcome(): List<TermLine> = listOf(
        TermLine("Linux 模拟终端 v1.0 —— 安全的练习沙盒", TermKind.SUCCESS),
        TermLine("所有操作只发生在 App 内部，不会影响你的手机。", TermKind.SYSTEM),
        TermLine("输入 help 查看支持的命令，输入 tasks 查看实战任务。", TermKind.SYSTEM),
        TermLine("", TermKind.OUTPUT)
    )

    // ---------- 历史 ----------
    fun previousHistory(current: String): String {
        if (history.isEmpty()) return current
        cursor = (cursor - 1).coerceIn(0, history.size)
        return history[cursor.coerceAtMost(history.size - 1)]
    }

    fun nextHistory(current: String): String {
        if (history.isEmpty()) return current
        cursor = (cursor + 1).coerceIn(0, history.size)
        return if (cursor >= history.size) "" else history[cursor]
    }

    // ---------- 执行 ----------
    fun run(input: String): RunResult {
        val raw = input.trim()
        if (raw.isEmpty()) return RunResult(emptyList())

        history += raw
        cursor = history.size

        val lines = mutableListOf<TermLine>()
        lines += TermLine(prompt() + raw, TermKind.COMMAND)

        var stdin = ""
        for (segment in splitPipe(raw)) {
            val (commandText, redirect) = splitRedirect(segment)
            val tokens = tokenize(commandText)
            if (tokens.isEmpty()) continue

            val result = dispatch(tokens.first(), tokens.drop(1), stdin)
            if (result.error != null) {
                lines += TermLine(result.error, TermKind.ERROR)
                return RunResult(lines)
            }
            if (result.signal == TermSignal.CLEAR) return RunResult(lines, TermSignal.CLEAR)

            if (redirect != null) {
                val target = redirect.file
                val content = if (redirect.append) (fs.node(target)?.content ?: "") + result.stdout else result.stdout
                if (!fs.write(target, content)) {
                    lines += TermLine(
                        "无法写入 $target：父目录不存在，或目标是一个目录",
                        TermKind.ERROR
                    )
                    return RunResult(lines)
                }
                stdin = ""
            } else {
                stdin = result.stdout
            }
        }

        if (stdin.isNotEmpty()) {
            lines(stdin).forEach { lines += TermLine(it, TermKind.OUTPUT) }
        }
        return RunResult(lines)
    }

    /** 与真实 shell 一致的行切分：结尾换行不产生多余空行 */
    private fun lines(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val t = if (text.endsWith("\n")) text.dropLast(1).removeSuffix("\r") else text
        return t.split("\r\n", "\n", "\r")
    }

    private data class ExecResult(
        val stdout: String = "",
        val error: String? = null,
        val signal: TermSignal = TermSignal.NONE
    )

    private data class Redirect(val file: String, val append: Boolean)

    // ---------- 词法 ----------
    private fun splitPipe(input: String): List<String> {
        val out = mutableListOf<String>()
        var buf = StringBuilder()
        var quote: Char? = null
        for (ch in input) {
            when {
                quote != null -> {
                    buf.append(ch)
                    if (ch == quote) quote = null
                }
                ch == '"' || ch == '\'' -> { quote = ch; buf.append(ch) }
                ch == '|' -> { out += buf.toString(); buf = StringBuilder() }
                else -> buf.append(ch)
            }
        }
        out += buf.toString()
        return out.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun splitRedirect(segment: String): Pair<String, Redirect?> {
        var i = 0
        var quote: Char? = null
        while (i < segment.length) {
            val ch = segment[i]
            if (quote != null) {
                if (ch == quote) quote = null
                i++
                continue
            }
            if (ch == '"' || ch == '\'') { quote = ch; i++; continue }
            if (ch == '>') {
                val append = i + 1 < segment.length && segment[i + 1] == '>'
                val filePart = segment.substring(if (append) i + 2 else i + 1).trim()
                val file = tokenize(filePart).firstOrNull() ?: ""
                return segment.substring(0, i).trim() to Redirect(file, append)
            }
            i++
        }
        return segment.trim() to null
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var buf = StringBuilder()
        var quote: Char? = null
        for (ch in input) {
            when {
                quote != null -> {
                    if (ch == quote) quote = null else buf.append(ch)
                }
                ch == '"' || ch == '\'' -> quote = ch
                ch.isWhitespace() -> {
                    if (buf.isNotEmpty()) { tokens += buf.toString(); buf = StringBuilder() }
                }
                else -> buf.append(ch)
            }
        }
        if (buf.isNotEmpty()) tokens += buf.toString()
        return tokens
    }

    // ---------- 命令分发 ----------
    private fun dispatch(name: String, args: List<String>, stdin: String): ExecResult = when (name) {
        "help", "man" -> help(args)
        "tasks" -> tasks()
        "pwd" -> ExecResult(fs.cwd.path() + "\n")
        "ls" -> ls(args)
        "cd" -> {
            val target = args.firstOrNull() ?: "~"
            if (fs.chdir(target)) ExecResult()
            else {
                val n = fs.node(target)
                if (n != null && !n.isDir) ExecResult(error = "cd: $target: 不是一个目录 (Not a directory)")
                else ExecResult(error = "cd: $target: 没有那个文件或目录 (No such file or directory)")
            }
        }
        "cat" -> {
            val (text, err) = readInput(args, stdin)
            if (err != null) ExecResult(error = "cat: $err") else ExecResult(text)
        }
        "echo" -> ExecResult(args.joinToString(" ") + "\n")
        "mkdir" -> mkdir(args)
        "touch" -> touch(args)
        "rm" -> rm(args)
        "rmdir" -> rmdir(args)
        "cp" -> cp(args)
        "mv" -> mv(args)
        "grep" -> grep(args, stdin)
        "head" -> headTail(args, stdin, head = true)
        "tail" -> headTail(args, stdin, head = false)
        "wc" -> wc(args, stdin)
        "sort" -> sortCmd(args, stdin)
        "uniq" -> uniq(args, stdin)
        "find" -> find(args)
        "tree" -> tree(args)
        "chmod" -> chmod(args)
        "whoami" -> ExecResult(fs.user + "\n")
        "hostname" -> ExecResult(fs.host + "\n")
        "date" -> ExecResult(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()) + "\n")
        "uname" -> ExecResult("Linux ${fs.host} 6.1.0-sandbox #1 SMP x86_64 GNU/Linux\n")
        "history" -> ExecResult(history.mapIndexed { i, h -> "  ${i + 1}  $h" }.joinToString("\n") + "\n")
        "clear" -> ExecResult(signal = TermSignal.CLEAR)
        else -> ExecResult(
            error = "$name: 未找到命令 (command not found)。输入 help 查看本沙盒支持的命令；" +
                "想查真实 Linux 用法，请回到「速查」页搜索 $name。"
        )
    }

    // ---------- 各命令实现 ----------
    private fun help(args: List<String>): ExecResult {
        val topic = args.firstOrNull()
        if (topic == null) {
            return ExecResult(
                """
本沙盒支持的命令：
  文件目录：ls  cd  pwd  mkdir  rmdir  touch  cp  mv  rm  tree  find  chmod
  查看文本：cat  head  tail  grep  wc  sort  uniq  echo
  系统其它：whoami  hostname  date  uname  history  clear  tasks  help

组合技：
  管道     命令1 | 命令2         把前一条的输出交给后一条
  重定向   命令 > 文件            输出保存到文件（覆盖）
           命令 >> 文件           输出追加到文件末尾

示例：
  ls -l | grep txt
  grep ERROR /var/log/app.log | wc -l
  echo "hello linux" > greeting.txt
  help ls         查看 ls 在真实 Linux 中的用法
                """.trimIndent() + "\n"
            )
        }
        val cmd = Repository.byName(topic)
            ?: return ExecResult(error = "沙盒里没有 '$topic' 这个命令，也未能匹配到命令库条目。")
        val sb = StringBuilder()
        sb.appendLine("${cmd.name} — ${cmd.zh}")
        sb.appendLine("${cmd.en}")
        if (cmd.syntax.isNotBlank()) sb.appendLine("语法：${cmd.syntax}")
        if (cmd.examples.isNotEmpty()) {
            sb.appendLine("示例：")
            cmd.examples.take(3).forEach { sb.appendLine("  ${it.code}   # ${it.zh}") }
        }
        return ExecResult(sb.toString())
    }

    private fun tasks(): ExecResult {
        val sb = StringBuilder()
        sb.appendLine("实战任务（完成 ${PracticeTasks.finished(this)}/${PracticeTasks.all.size}）：")
        PracticeTasks.all.forEach { task ->
            val mark = if (task.done(this)) "[✓]" else "[ ]"
            sb.appendLine(" $mark ${task.title} —— ${task.hint}")
            sb.appendLine("      试试：${task.example}")
        }
        sb.appendLine("提示：完成后状态会自动更新；点上方任务卡片可一键填入命令。")
        return ExecResult(sb.toString())
    }

    private fun ls(args: List<String>): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val targets = args.filter { !it.startsWith("-") }.ifEmpty { listOf(".") }
        val showAll = flags.any { it.contains('a') }
        val long = flags.any { it.contains('l') }
        val sb = StringBuilder()

        targets.forEachIndexed { index, target ->
            val node = fs.node(target)
            if (node == null) {
                return ExecResult(error = "ls: 无法访问 '$target': 没有那个文件或目录")
            }
            if (targets.size > 1) {
                if (index > 0) sb.append('\n')
                sb.appendLine("$target:")
            }
            if (!node.isDir) {
                sb.appendLine(formatEntry(node, long))
                return@forEachIndexed
            }
            val entries = fs.childrenOf(node)
                .filter { showAll || !it.name.startsWith(".") }
            if (long) {
                sb.appendLine("总用量 ${entries.size}")
                entries.forEach { sb.appendLine(formatEntry(it, true)) }
            } else {
                sb.appendLine(entries.joinToString("  ") { if (it.isDir) "${it.name}/" else it.name })
            }
        }
        return ExecResult(sb.toString())
    }

    private fun formatEntry(node: FsNode, long: Boolean): String =
        if (long) {
            val type = if (node.isDir) "d" else "-"
            val size = if (node.isDir) 4096 else (node.content ?: "").length
            "$type${node.mode}  ${node.name.padEnd(20)} ${size.toString().padStart(6)}"
        } else {
            if (node.isDir) "${node.name}/" else node.name
        }

    private fun mkdir(args: List<String>): ExecResult {
        val parents = args.any { it.startsWith("-") && it.contains('p') }
        val dirs = args.filter { !it.startsWith("-") }
        if (dirs.isEmpty()) return ExecResult(error = "mkdir: 缺少操作数（用法：mkdir [-p] 目录名）")
        dirs.forEach { dir ->
            val exists = fs.exists(dir)
            if (exists) {
                if (!parents) return ExecResult(error = "mkdir: 无法创建目录 '$dir': 文件已存在")
                return@forEach
            }
            if (parents) {
                if (fs.mkdirp(dir) == null) return ExecResult(error = "mkdir: 无法创建目录 '$dir'")
            } else {
                val parent = fs.parentOf(dir) ?: return ExecResult(error = "mkdir: 无法创建目录 '$dir': 父目录不存在")
                if (!parent.isDir) return ExecResult(error = "mkdir: 无法创建目录 '$dir': 父路径不是目录")
                val name = fs.nameOf(dir)
                if (name.isEmpty() || name.contains('/')) return ExecResult(error = "mkdir: 目录名非法")
                parent.children[name] = FsNode(name, parent, null)
            }
        }
        return ExecResult()
    }

    private fun touch(args: List<String>): ExecResult {
        val files = args.filter { !it.startsWith("-") }
        if (files.isEmpty()) return ExecResult(error = "touch: 缺少文件名（用法：touch 文件名）")
        files.forEach { file ->
            val node = fs.node(file)
            if (node == null) {
                if (!fs.write(file, "")) return ExecResult(error = "touch: 无法创建 '$file': 父目录不存在")
            } else if (node.isDir) {
                return ExecResult(error = "touch: '$file' 是一个目录")
            }
        }
        return ExecResult()
    }

    private fun rm(args: List<String>): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val recursive = flags.any { it.contains('r') || it.contains('R') }
        val force = flags.any { it.contains('f') }
        val targets = args.filter { !it.startsWith("-") }
        if (targets.isEmpty()) {
            return if (force) ExecResult() else ExecResult(error = "rm: 缺少操作数（用法：rm [-r] 文件或目录）")
        }
        targets.forEach { target ->
            val node = fs.node(target)
            if (node == null) {
                if (!force) return ExecResult(error = "rm: 无法删除 '$target': 没有那个文件或目录")
                return@forEach
            }
            if (node.isDir && !recursive) {
                return ExecResult(error = "rm: 无法删除 '$target': 是一个目录（加 -r 递归删除）")
            }
            if (!fs.remove(target, recursive)) {
                return ExecResult(error = "rm: 无法删除 '$target'")
            }
        }
        return ExecResult()
    }

    private fun rmdir(args: List<String>): ExecResult {
        val dirs = args.filter { !it.startsWith("-") }
        if (dirs.isEmpty()) return ExecResult(error = "rmdir: 缺少操作数（用法：rmdir 空目录）")
        dirs.forEach { dir ->
            val node = fs.node(dir) ?: return ExecResult(error = "rmdir: 无法删除 '$dir': 没有那个文件或目录")
            if (!node.isDir) return ExecResult(error = "rmdir: 无法删除 '$dir': 不是一个目录")
            if (node.children.isNotEmpty()) return ExecResult(error = "rmdir: 无法删除 '$dir': 目录非空（用 rm -r）")
            fs.remove(dir, false)
        }
        return ExecResult()
    }

    private fun cp(args: List<String>): ExecResult {
        val recursive = args.any { it.startsWith("-") && (it.contains('r') || it.contains('R')) }
        val rest = args.filter { !it.startsWith("-") }
        if (rest.size < 2) return ExecResult(error = "cp: 用法：cp [-r] 源 目标")
        val src = fs.node(rest[0]) ?: return ExecResult(error = "cp: 无法获取 '${rest[0]}': 没有那个文件或目录")
        if (src.isDir && !recursive) return ExecResult(error = "cp: 略过目录 '${rest[0]}'（复制目录要加 -r）")
        return if (fs.copy(src, rest[1], recursive)) ExecResult()
        else ExecResult(error = "cp: 无法复制到 '${rest[1]}'")
    }

    private fun mv(args: List<String>): ExecResult {
        val rest = args.filter { !it.startsWith("-") }
        if (rest.size < 2) return ExecResult(error = "mv: 用法：mv 源 目标")
        if (!fs.exists(rest[0])) return ExecResult(error = "mv: 无法获取 '${rest[0]}': 没有那个文件或目录")
        return if (fs.move(rest[0], rest[1])) ExecResult()
        else ExecResult(error = "mv: 无法移动到 '${rest[1]}'")
    }

    private fun grep(args: List<String>, stdin: String): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val rest = args.filter { !it.startsWith("-") }
        if (rest.isEmpty()) return ExecResult(error = "grep: 用法：grep [-i] [-n] [-v] [-c] 关键字 [文件]")
        val pattern = rest[0]
        val files = rest.drop(1)
        val ignoreCase = flags.any { it.contains('i') }
        val showNumber = flags.any { it.contains('n') }
        val invert = flags.any { it.contains('v') }
        val countOnly = flags.any { it.contains('c') }

        val (text, err) = readInput(files, stdin)
        if (err != null) return ExecResult(error = "grep: $err")

        val regex = try {
            Regex(pattern, if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
        } catch (e: Exception) {
            Regex(Regex.escape(pattern), if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
        }

        val lines = lines(text)
        var matched = 0
        val sb = StringBuilder()
        lines.forEachIndexed { index, line ->
            val hit = regex.containsMatchIn(line)
            if (hit != invert) {
                matched++
                if (!countOnly) {
                    if (showNumber) sb.appendLine("${index + 1}:$line") else sb.appendLine(line)
                }
            }
        }
        if (countOnly) sb.appendLine("$matched")
        return ExecResult(sb.toString())
    }

    private fun headTail(args: List<String>, stdin: String, head: Boolean): ExecResult {
        val rest = args.filter { !it.startsWith("-") }
        var count = 10
        val nFlagIndex = args.indexOfFirst { it == "-n" }
        if (nFlagIndex >= 0 && nFlagIndex + 1 < args.size) {
            count = args[nFlagIndex + 1].toIntOrNull() ?: 10
        } else {
            args.filter { it.startsWith("-") }.forEach { flag ->
                val v = flag.removePrefix("-").toIntOrNull()
                if (v != null) count = v
            }
        }
        val files = if (nFlagIndex >= 0) rest.filter { it != args[nFlagIndex + 1] } else rest
        val (text, err) = readInput(files, stdin)
        if (err != null) return ExecResult(error = "${if (head) "head" else "tail"}: $err")
        val lines = lines(text)
        val picked = if (head) lines.take(count) else lines.takeLast(count)
        return ExecResult(picked.joinToString("\n", postfix = if (picked.isEmpty()) "" else "\n"))
    }

    private fun wc(args: List<String>, stdin: String): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val files = args.filter { !it.startsWith("-") }
        val (text, err) = readInput(files, stdin)
        if (err != null) return ExecResult(error = "wc: $err")
        val lines = lines(text).size
        val words = text.split(Regex("\\s+")).count { it.isNotEmpty() }
        val chars = text.length
        val value = when {
            flags.any { it.contains('l') } -> lines
            flags.any { it.contains('w') } -> words
            flags.any { it.contains('c') } -> chars
            else -> lines
        }
        return if (flags.isEmpty()) ExecResult("$lines $words $chars\n")
        else ExecResult("$value\n")
    }

    private fun sortCmd(args: List<String>, stdin: String): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val files = args.filter { !it.startsWith("-") }
        val (text, err) = readInput(files, stdin)
        if (err != null) return ExecResult(error = "sort: $err")
        var lines = lines(text)
        lines = if (flags.any { it.contains('n') }) {
            lines.sortedBy { it.trim().toLongOrNull() ?: Long.MAX_VALUE }
        } else {
            lines.sorted()
        }
        if (flags.any { it.contains('r') }) lines = lines.reversed()
        if (flags.any { it.contains('u') }) lines = lines.distinct()
        return ExecResult(lines.joinToString("\n", postfix = "\n"))
    }

    private fun uniq(args: List<String>, stdin: String): ExecResult {
        val flags = args.filter { it.startsWith("-") }
        val files = args.filter { !it.startsWith("-") }
        val (text, err) = readInput(files, stdin)
        if (err != null) return ExecResult(error = "uniq: $err")
        val countMode = flags.any { it.contains('c') }
        val sb = StringBuilder()
        var previous: String? = null
        var count = 0
        fun flush() {
            previous?.let {
                if (countMode) sb.appendLine("    $count $it") else sb.appendLine(it)
            }
        }
        lines(text).forEach { line ->
            if (line == previous) count++ else {
                flush()
                previous = line
                count = 1
            }
        }
        flush()
        return ExecResult(sb.toString())
    }

    private fun find(args: List<String>): ExecResult {
        val startPath = args.firstOrNull { !it.startsWith("-") } ?: "."
        val nameIndex = args.indexOf("-name")
        val pattern = if (nameIndex >= 0 && nameIndex + 1 < args.size) args[nameIndex + 1] else null
        val typeIndex = args.indexOf("-type")
        val type = if (typeIndex >= 0 && typeIndex + 1 < args.size) args[typeIndex + 1] else null

        val start = fs.node(startPath) ?: return ExecResult(error = "find: '$startPath': 没有那个文件或目录")
        val all = mutableListOf(start)
        if (start.isDir) fs.walk(start, includeDirs = true, all)

        val regex = pattern?.let { p ->
            Regex("^" + p.replace(".", "\\.").replace("*", ".*").replace("?", ".") + "\$")
        }
        val sb = StringBuilder()
        all.forEach { node ->
            val typeOk = when (type) {
                "f" -> !node.isDir
                "d" -> node.isDir
                else -> true
            }
            val nameOk = regex?.containsMatchIn(node.name) ?: true
            if (typeOk && nameOk) {
                val p = node.path()
                sb.appendLine(if (p.startsWith(fs.cwd.path())) p else p)
            }
        }
        return ExecResult(sb.toString())
    }

    private fun tree(args: List<String>): ExecResult {
        val target = args.firstOrNull { !it.startsWith("-") } ?: "."
        val start = fs.node(target) ?: return ExecResult(error = "tree: '$target': 没有那个文件或目录")
        val sb = StringBuilder()
        sb.appendLine(start.path())
        fun walk(node: FsNode, prefix: String) {
            val entries = fs.childrenOf(node)
            entries.forEachIndexed { index, child ->
                val last = index == entries.lastIndex
                sb.appendLine(prefix + (if (last) "└── " else "├── ") + child.name + if (child.isDir) "/" else "")
                if (child.isDir) walk(child, prefix + if (last) "    " else "│   ")
            }
        }
        walk(start, "")
        return ExecResult(sb.toString())
    }

    private fun chmod(args: List<String>): ExecResult {
        val rest = args.filter { !it.startsWith("-") }
        if (rest.size < 2) return ExecResult(error = "chmod: 用法：chmod 755 文件或目录")
        val mode = modeOf(rest[0]) ?: return ExecResult(error = "chmod: 无效模式 '${rest[0]}'（试试 755、644）")
        val node = fs.node(rest[1]) ?: return ExecResult(error = "chmod: 无法访问 '${rest[1]}': 没有那个文件或目录")
        node.mode = mode
        return ExecResult()
    }

    private fun modeOf(octal: String): String? {
        if (octal.length != 3 || octal.any { it !in '0'..'7' }) return null
        return octal.map { digit ->
            val v = digit - '0'
            (if (v and 4 != 0) "r" else "-") +
                (if (v and 2 != 0) "w" else "-") +
                (if (v and 1 != 0) "x" else "-")
        }.joinToString("")
    }

    /** 从文件参数或标准输入读取文本 */
    private fun readInput(files: List<String>, stdin: String): Pair<String, String?> {
        if (files.isEmpty()) return stdin to null
        val sb = StringBuilder()
        for ((index, file) in files.withIndex()) {
            val node = fs.node(file)
            if (node == null) return "" to "$file: 没有那个文件或目录"
            if (node.isDir) return "" to "$file: 是一个目录"
            if (index > 0) sb.append('\n')
            sb.append(node.content ?: "")
        }
        return sb.toString() to null
    }
}
