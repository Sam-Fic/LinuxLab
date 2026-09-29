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

package com.linuxlab.starter.data

import com.linuxlab.starter.model.Level

/** 搜索与文本处理 —— Linux 真正的生产力来源 */
val SearchGroup = group("search", "搜索与文本处理", "Search & Text Processing", "search") {

    c("grep", "在文本中搜索关键字", "Search text using patterns", "grep [选项] '模式' [文件...]", level = Level.ADVANCED) {
        detail(
            "grep 按行匹配，输出包含模式的整行。它支持基础正则表达式，配合管道可以层层过滤数据。",
            "grep matches line by line and prints matching lines. With regular expressions and pipes it becomes a data filter."
        )
        p("-i", "忽略大小写", "Case-insensitive matching")
        p("-v", "反选：输出不匹配的行", "Invert: print non-matching lines")
        p("-n", "显示行号", "Prefix each line with its number")
        p("-r / -R", "递归搜索目录", "Search directories recursively")
        p("-l", "只显示包含匹配的文件名", "List matching filenames only")
        p("-c", "只统计匹配的行数", "Print only a count of matches")
        p("-E", "使用扩展正则（等同 egrep）", "Use extended regex (like egrep)")
        p("-A/-B/-C N", "额外显示匹配行之后 / 之前 / 前后各 N 行", "Show N lines after / before / around each match")
        p("-w", "全词匹配", "Match whole words only")
        e("grep 'error' app.log", "在日志里找 error", "Find 'error' in a log file")
        e("grep -rn 'TODO' src/", "递归查找源码里的 TODO 并带行号", "Find TODOs in source code with line numbers")
        e("grep -v '^#' nginx.conf | grep -v '^\$'", "去掉注释行和空行", "Strip comments and blank lines")
        e("dmesg | grep -i usb", "从内核日志里找 USB 相关信息", "Look for USB messages in the kernel log")
        e("grep -E 'error|warn|fail' *.log", "一次匹配多个关键词", "Match several keywords at once")
        tip("正则三宝：^ 行首、\$ 行尾、. 任意字符、* 前一个字符重复任意次。")
        rel("egrep", "fgrep", "awk", "sed", "find")
    }

    c("egrep", "扩展正则搜索", "Search with extended regular expressions", "egrep [选项] '模式' 文件", level = Level.ADVANCED) {
        detail("相当于 grep -E，支持 + ? | () 等扩展正则运算符。", "Same as grep -E: supports +, ?, | and grouping.")
        e("egrep 'warn|error' app.log", "同时查找 warn 和 error", "Find warn or error lines")
        e("egrep '^[0-9]{1,3}\\.' access.log", "找出以 IP 开头的行", "Find lines starting with an IP")
        tip("新脚本里推荐直接写 grep -E，兼容性更好。")
        rel("grep", "fgrep")
    }

    c("fgrep", "纯字符串快速搜索", "Search for fixed strings, no regex", "fgrep [选项] '字符串' 文件") {
        detail("相当于 grep -F：不解析正则，搜索含 . * [ ] 等符号的文本时更快更安全。", "Same as grep -F: no regex parsing, faster and safer for literal text with special characters.")
        e("fgrep '192.168.1.1' access.log", "按字面搜索 IP", "Search an IP literally")
        e("fgrep -f keys.txt data.txt", "把 keys.txt 每行当作关键字批量匹配", "Use each line of keys.txt as a pattern")
        rel("grep", "egrep")
    }

    c("sed", "流编辑器：批量替换与编辑", "Stream editor for filtering and transforming text", "sed [选项] '脚本' 文件", level = Level.ADVANCED) {
        detail(
            "sed 逐行处理文本，最常用的场景是「批量替换」。加 -i 才会真正修改文件，否则只输出到屏幕。",
            "sed processes text line by line; the classic use is bulk substitution. Nothing is written to disk unless you add -i."
        )
        p("s/旧/新/", "替换每行第一个匹配", "Replace the first match on each line")
        p("s/旧/新/g", "替换每行所有匹配", "Replace every match on each line")
        p("-i", "直接修改文件内容", "Edit files in place")
        p("-i.bak", "修改前先备份成 .bak", "Edit in place and keep a .bak backup")
        p("-n", "静默模式，配合 p 只打印需要的行", "Suppress automatic printing (use with p)")
        p("-e", "串联多个编辑命令", "Add several expressions")
        p("d", "删除匹配的行", "Delete matching lines")
        e("sed 's/foo/bar/g' file.txt", "把 foo 全部替换为 bar（只显示不保存）", "Replace all foo with bar (preview only)")
        e("sed -i 's/http:/https:/g' *.conf", "批量把配置改成 https", "Switch every config to https")
        e("sed -n '10,20p' file.txt", "只打印第 10 到 20 行", "Print lines 10 to 20")
        e("sed '/^#/d;/^\$/d' nginx.conf", "删除注释行和空行", "Delete comments and blank lines")
        e("sed -i.bak 's/old/new/' a.txt", "修改前自动备份", "Edit in place with a backup")
        tip("替换路径时可用其他分隔符避免转义：sed 's#/a/b#/c/d#'")
        rel("grep", "awk", "tr", "vim")
    }

    c("awk", "按列处理文本的编程语言", "Pattern scanning and processing language", "awk [选项] '模式 {动作}' 文件", level = Level.ADVANCED) {
        detail(
            "awk 把每行按空格切成列：\$1 第一列、\$2 第二列、\$NF 最后一列、\$0 整行。既能过滤也能统计，是终端里的轻量数据处理语言。",
            "awk splits each line into fields: \$1, \$2, \$NF (last) and \$0 (whole line). It filters, computes and reports — a tiny data language in your terminal."
        )
        p("-F", "指定输入分隔符，如 -F','", "Set the input field separator")
        p("\$0 / \$1", "整行 / 第 1 列", "Whole line / first field")
        p("\$NF", "最后一列", "The last field")
        p("NR", "当前行号", "Number of the current record")
        p("BEGIN{} / END{}", "处理前 / 处理完后执行", "Run before / after processing")
        p("print / printf", "输出内容 / 格式化输出", "Print / print with format")
        e("awk '{print \$1}' access.log", "取出日志的第一列（IP）", "Print the first column (IPs)")
        e("awk -F: '{print \$1, \$3}' /etc/passwd", "以冒号分隔，列出用户名和 UID", "List user names and UIDs")
        e("awk '\$3 > 1000 {print \$1}' /etc/passwd", "找出 UID 大于 1000 的用户", "Find users with UID > 1000")
        e("awk '{sum += \$1} END {print sum}' nums.txt", "求第一列的总和", "Sum the first column")
        e("ps aux | awk 'NR>1 {print \$3, \$4, \$11}'", "列出进程的 CPU、内存和命令", "Show CPU, memory and command of processes")
        e("awk 'length(\$0) > 80 {print FILENAME, NR}' *.md", "找出超过 80 列的长行", "Find lines longer than 80 chars")
        tip("取某列最常用：awk '{print \$N}'；做统计用 BEGIN/END 累加。")
        rel("grep", "cut", "sed", "sort")
    }

    c("xargs", "把管道数据变成命令参数", "Build and execute command lines from stdin", "xargs [选项] [命令]") {
        detail("很多命令不接受管道输入（如 rm、kill），xargs 负责把它们转换成参数。", "Many commands (rm, kill) do not read stdin; xargs turns input into arguments.")
        p("-n N", "每次传 N 个参数", "Use at most N arguments per command line")
        p("-I{}", "用占位符 {} 把参数放到指定位置", "Replace {} with the input line")
        p("-0", "配合 find -print0 处理带空格的文件名", "Input items are terminated by a null character")
        p("-p", "执行前逐个确认", "Prompt before running each command")
        p("-t", "先打印要执行的命令", "Print the command before running it")
        e("find . -name '*.tmp' | xargs rm -f", "批量删除找到的临时文件", "Delete found temp files in bulk")
        e("cat urls.txt | xargs -n 1 curl -O", "逐行下载 URL 列表里的文件", "Download every URL, one per command")
        e("ls *.txt | xargs -I{} cp {} {}.bak", "给每个 txt 文件做备份", "Back up every .txt file")
        e("find . -name '*.log' -print0 | xargs -0 rm", "安全处理含空格的文件名", "Safely handle filenames with spaces")
        tip("文件名可能含空格时，务必用 find -print0 | xargs -0 组合。")
        rel("find", "grep", "awk")
    }

    c("tee", "一边显示一边保存", "Read stdin and write to files and stdout", "命令 | tee [选项] 文件") {
        detail("管道中的「三通」：把结果同时输出到屏幕和文件。", "A T-junction for pipes: show the output and save it at the same time.")
        p("-a", "追加写入而不是覆盖", "Append instead of overwriting")
        e("ls -l | tee listing.txt", "把列表结果保存下来同时查看", "Save a listing while viewing it")
        e("echo 'x' | sudo tee -a /etc/hosts", "向 root 权限的文件追加内容", "Append to a root-owned file")
        tip("sudo echo > file 不起作用（重定向发生在提权之前），要用 sudo tee。")
        rel("echo", "cat")
    }

    c("strings", "从二进制里提取可读文本", "Extract printable strings from files", "strings [选项] 文件") {
        p("-n N", "只显示长度不少于 N 的字符串", "Only show strings of at least N characters")
        e("strings /bin/ls | grep 'usage'", "在可执行文件中查找提示文本", "Search a binary for a message")
        rel("grep", "file")
    }

    c("jq", "处理 JSON 数据", "A lightweight JSON processor", "jq [选项] '过滤器' 文件", level = Level.ADVANCED) {
        detail("命令行里的 JSON 瑞士军刀：格式化、取值、过滤、重组。", "The Swiss army knife for JSON on the command line: pretty-print, extract, filter and reshape.")
        p(".", "原样格式化输出", "Pretty-print the input")
        p(".key", "取出某个字段", "Extract a field")
        p(".[]", "遍历数组元素", "Iterate over array items")
        p("-r", "输出原始字符串（去掉引号）", "Output raw strings without quotes")
        p("select()", "按条件过滤", "Filter by a condition")
        e("curl -s api.github.com/users/alex | jq '.name'", "取出接口返回里的 name 字段", "Extract the name field from an API response")
        e("jq '.items[] | {id, title}' data.json", "重组数组里的字段", "Reshape array entries")
        e("cat logs.jsonl | jq -r 'select(.level==\"error\") | .msg'", "只输出错误日志的消息", "Print only error messages")
        tip("没装 jq 时可用 python3 -m json.tool 做简单格式化。")
        rel("curl", "awk", "grep")
    }

    c("column", "把输出排成对齐的表格", "Columnate lists into tables", "column [选项] 文件") {
        p("-t", "自动按列对齐", "Create a table from the input")
        p("-s", "指定输入分隔符", "Set the input delimiter")
        e("mount | column -t", "让挂载信息整齐对齐", "Align mount output into a table")
        e("cat -A /etc/passwd | column -t -s:", "按冒号分列显示", "Show /etc/passwd as columns")
        rel("awk", "paste")
    }

    c("rev", "反转每行字符", "Reverse the characters of each line", "rev 文件") {
        e("echo 'abc' | rev", "输出 cba", "Prints cba")
        e("echo '/var/log/app.log' | rev | cut -d/ -f1 | rev", "取出最后一段文件名", "Extract the last path segment")
        rel("tac", "cut", "basename")
    }

    c("comm", "对比两个已排序文件的异同", "Compare two sorted files line by line", "comm [选项] 文件1 文件2") {
        detail("三列输出：只在文件1、只在文件2、两者共有。文件必须先排序。", "Three columns: only in file 1, only in file 2, and common lines. Input must be sorted.")
        p("-1/-2/-3", "隐藏对应的列", "Suppress the given column")
        e("comm -12 <(sort a.txt) <(sort b.txt)", "求两个文件的交集", "Print the intersection of two files")
        e("comm -23 <(sort a.txt) <(sort b.txt)", "求只在 a.txt 中出现的行", "Print lines only in a.txt")
        rel("diff", "sort", "uniq")
    }

    c("expand", "把 Tab 转成空格", "Convert tabs to spaces", "expand [选项] 文件") {
        p("-t N", "指定 Tab 宽度（默认 8）", "Set the tab width (default 8)")
        e("expand -t 4 file.py", "把制表符转换成 4 个空格", "Convert tabs to four spaces")
        rel("unexpand", "tr")
    }

    c("split", "拆分大文件", "Split a file into pieces", "split [选项] 文件 [前缀]") {
        p("-l N", "每 N 行切一片", "Put N lines per output file")
        p("-b N", "每 N 字节切一片，如 100M", "Put N bytes per output file (e.g. 100M)")
        p("-d", "用数字后缀（x00 x01）", "Use numeric suffixes")
        e("split -l 1000 big.csv part_", "每 1000 行拆成一个文件", "Split a CSV into 1000-line chunks")
        e("cat part_* > big.csv", "把切片合并回原文件", "Reassemble the pieces")
        rel("cat", "csplit", "tar")
    }
}
