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

/** 文本查看与编辑 —— 在终端里读文件、写文件的必备命令 */
val TextViewGroup = group("text", "文本查看与编辑", "Viewing & Editing Text", "article") {

    c("cat", "显示整个文件的内容", "Concatenate and print files", "cat [选项] 文件...") {
        detail("cat 把文件内容连续输出到屏幕；文件很短时用 cat，长文件请用 less 分页查看。", "cat dumps the whole file to the screen. Use it for short files; use less for long ones.")
        p("-n", "显示行号", "Number all output lines")
        p("-b", "只对非空行编号", "Number non-blank lines only")
        p("-A", "显示不可见字符（行尾 \$、Tab ^I）", "Show invisible characters")
        p("-s", "压缩连续的空行为一行", "Squeeze repeated blank lines")
        e("cat hello.txt", "查看 hello.txt 的内容", "Print the content of hello.txt")
        e("cat a.txt b.txt > all.txt", "把两个文件合并成 all.txt", "Merge two files into all.txt")
        e("cat -n /etc/passwd | head", "带行号查看系统账号文件前 10 行", "Show the first 10 lines of /etc/passwd with numbers")
        tip("千万不要 cat 一个几个 G 的日志文件，用 less 或 tail -f。")
        rel("less", "head", "tail", "tac")
    }

    c("less", "分页查看长文件（推荐）", "View a file page by page", "less [选项] 文件") {
        detail("比 more 更强大的分页器：可上下翻页、搜索、跳转，且不会一次性读完整个文件。", "A pager that lets you scroll, search and jump without loading the whole file into memory.")
        p("空格/f", "向下翻一页", "Forward one page")
        p("b", "向上翻一页", "Back one page")
        p("/关键词", "向下搜索，n 找下一个", "Search forward; n repeats")
        p("?关键词", "向上搜索", "Search backward")
        p("g / G", "跳到开头 / 结尾", "Jump to the start / end")
        p("q", "退出", "Quit")
        e("less /var/log/syslog", "分页查看系统日志", "Read the system log page by page")
        e("less +F app.log", "像 tail -f 一样实时跟踪日志", "Follow a log like tail -f")
        tip("less 里按 h 可以看完整快捷键帮助。")
        rel("more", "head", "tail", "cat")
    }

    c("more", "简单的分页查看器", "A simple file pager", "more [选项] 文件") {
        detail("老式分页器，只能向下翻页，几乎所有系统都自带。", "An older pager that only moves forward, available everywhere.")
        p("-N", "显示行号", "Show line numbers")
        p("+N", "从第 N 行开始显示", "Start at line N")
        e("more -N README.md", "带行号逐屏查看", "Show README with line numbers")
        tip("能用 less 就用 less，功能更全。")
        rel("less", "cat")
    }

    c("head", "查看文件开头几行", "Show the first lines of a file", "head [选项] 文件") {
        p("-n N", "显示前 N 行（默认 10 行）", "Show the first N lines (default 10)")
        p("-c N", "显示前 N 个字节", "Show the first N bytes")
        p("-q", "多文件时不显示文件名标题", "Never print file name headers")
        e("head -n 20 access.log", "查看日志前 20 行", "Show the first 20 log lines")
        e("head -c 100 image.png | xxd", "查看文件前 100 字节的十六进制", "Inspect the first 100 bytes in hex")
        rel("tail", "less", "sed")
    }

    c("tail", "查看文件结尾几行", "Show the last lines of a file", "tail [选项] 文件") {
        detail("看日志的利器，-f 可以实时盯着文件新增的内容。", "The go-to log tool; -f keeps watching as new lines arrive.")
        p("-n N", "显示最后 N 行", "Show the last N lines")
        p("-f", "实时跟踪文件新增内容", "Follow the file as it grows")
        p("-F", "跟踪并支持文件轮转（日志切割后自动重连）", "Follow and retry if the file is rotated")
        p("-c N", "显示最后 N 个字节", "Show the last N bytes")
        e("tail -n 50 app.log", "查看日志最后 50 行", "Show the last 50 lines")
        e("tail -f /var/log/nginx/access.log", "实时观察网站访问日志", "Watch web access logs live")
        e("tail -n +100 file.txt", "从第 100 行显示到结尾", "Print from line 100 to the end")
        tip("多个日志一起看：tail -f a.log b.log，或者配合 multitail / lnav。")
        rel("head", "less", "journalctl")
    }

    c("tac", "倒序显示文件内容", "Print a file in reverse line order", "tac [选项] 文件") {
        detail("名字就是 cat 反过来：从最后一行开始显示。", "cat spelled backwards: prints lines from the last to the first.")
        p("-s", "指定分隔符（默认是换行）", "Use a custom separator instead of newline")
        e("tac log.txt | head", "快速看文件最后几行（按行倒序）", "Quickly see the newest entries")
        rel("cat", "rev", "tail")
    }

    c("nl", "带行号显示文件", "Number the lines of a file", "nl [选项] 文件") {
        p("-b a", "给所有行编号（含空行）", "Number all lines, including blanks")
        p("-b t", "只给非空行编号（默认）", "Number non-empty lines only (default)")
        p("-w N", "设置行号宽度", "Set the line-number width")
        e("nl -ba script.sh", "给脚本每一行编号", "Number every line of a script")
        rel("cat", "head")
    }

    c("wc", "统计行数 / 单词数 / 字节数", "Count lines, words and bytes", "wc [选项] 文件") {
        detail("输出顺序为：行数 单词数 字节数 文件名。", "Output order: lines, words, bytes, filename.")
        p("-l", "只统计行数（最常用）", "Count lines only (most common)")
        p("-w", "只统计单词数", "Count words only")
        p("-c", "只统计字节数", "Count bytes only")
        p("-m", "统计字符数（考虑多字节）", "Count characters, honouring multibyte")
        e("wc -l access.log", "统计日志总行数", "Count how many lines the log has")
        e("cat *.md | wc -w", "统计所有 Markdown 文档的总词数", "Count words across all Markdown files")
        e("ls | wc -l", "统计当前目录有多少个条目", "Count entries in the current directory")
        rel("cat", "grep", "awk")
    }

    c("sort", "对文本行排序", "Sort lines of text", "sort [选项] 文件") {
        detail("默认按字典序排序；排序不会改动原文件，除非用 -o 写回。", "Sorts lexicographically by default. The source file is untouched unless you use -o.")
        p("-n", "按数值大小排序", "Sort numerically")
        p("-r", "倒序排序", "Sort in reverse")
        p("-k N", "按第 N 列排序（配合 -t 指定分隔符）", "Sort by column N (with -t)")
        p("-t", "指定列分隔符，如 -t','", "Set the field separator")
        p("-u", "排序并去重", "Sort and remove duplicates")
        p("-h", "识别 1K / 2G 这类人类可读数字", "Compare human-readable numbers (1K, 2G)")
        e("sort names.txt", "按字母顺序排序", "Sort alphabetically")
        e("sort -t: -k3 -n /etc/passwd", "按 UID 数字排序用户", "Sort users by numeric UID")
        e("du -h | sort -h | tail", "按大小排序目录占用", "Sort directory sizes")
        rel("uniq", "awk", "cut")
    }

    c("uniq", "去掉相邻重复行", "Filter adjacent duplicate lines", "uniq [选项] [文件]") {
        detail("只处理相邻的重复行，所以通常先 sort 再 uniq。", "Only collapses *adjacent* duplicates, so you usually pipe sort first.")
        p("-c", "统计每行重复次数", "Prefix lines with the count")
        p("-d", "只显示重复过的行", "Only print duplicated lines")
        p("-u", "只显示没重复的行", "Only print unique lines")
        p("-i", "比较时忽略大小写", "Ignore case when comparing")
        e("sort access.log | uniq -c | sort -nr | head", "统计出现最多的记录（Top N 经典写法）", "Classic top-N frequency count")
        e("uniq -d dup.txt", "找出重复的行", "Find duplicated lines")
        tip("统计 Top N 的三板斧：sort | uniq -c | sort -nr | head")
        rel("sort", "wc", "awk")
    }

    c("cut", "按列截取文本", "Extract columns from each line", "cut [选项] 文件") {
        p("-d", "指定分隔符（默认 Tab）", "Set the delimiter (default TAB)")
        p("-f N", "取第 N 列，如 -f1,3 或 -f2-4", "Select fields: -f1,3 or -f2-4")
        p("-c N", "按字符位置截取", "Select by character position")
        p("--complement", "取反：输出指定列之外的部分", "Print everything except the selected fields")
        e("cut -d: -f1 /etc/passwd", "列出所有用户名", "List all user names")
        e("echo 'a,b,c' | cut -d, -f2", "输出 b", "Prints b")
        e("ls -l | cut -c1-10", "只看权限位这一列", "Show only the permission column")
        rel("awk", "paste", "tr")
    }

    c("paste", "按列合并多个文件", "Merge lines of files side by side", "paste [选项] 文件...") {
        p("-d", "指定连接符，默认是 Tab", "Set the delimiter (default TAB)")
        p("-s", "把一个文件的所有行合并成一行", "Paste one file at a time (serial)")
        e("paste a.txt b.txt", "把两个文件并排合并", "Join two files column-wise")
        e("paste -d, -s names.txt", "把列表转成逗号分隔的一行", "Turn a list into one CSV line")
        rel("cut", "join", "awk")
    }

    c("join", "按共同字段连接两个文件", "Join lines of two files on a common field", "join [选项] 文件1 文件2") {
        detail("类似数据库的 JOIN：两文件必须先按连接字段排序。", "Like a SQL JOIN: both files must be sorted on the join field.")
        p("-t", "指定字段分隔符", "Set the field separator")
        p("-1 / -2", "指定文件 1 / 文件 2 用第几列连接", "Choose the join field of file 1 / file 2")
        e("join -t, users.csv orders.csv", "按第一列连接用户和订单", "Join users and orders on column 1")
        rel("paste", "sort", "awk")
    }

    c("tr", "字符替换 / 删除 / 压缩", "Translate or delete characters", "tr [选项] 集合1 [集合2]") {
        detail("按字符集合处理，不支持正则表达式；替换是一个字符对一个字符。", "Works on character sets, one char at a time — not regular expressions.")
        p("-d", "删除集合中的字符", "Delete characters in set 1")
        p("-s", "压缩重复字符", "Squeeze repeats into one")
        p("-c", "取反：处理集合之外的字符", "Complement set 1")
        e("echo 'hello' | tr a-z A-Z", "转成大写", "Convert to upper case")
        e("cat file.txt | tr -d '\\r'", "去掉 Windows 换行符", "Strip carriage returns")
        e("echo 'a   b' | tr -s ' '", "把连续空格压缩成一个", "Squeeze repeated spaces")
        rel("sed", "cut", "awk")
    }

    c("diff", "比较两个文件的差异", "Compare files line by line", "diff [选项] 文件1 文件2") {
        detail("输出中 < 表示左边文件独有，> 表示右边文件独有。", "In the output, '<' marks lines only in the first file and '>' lines only in the second.")
        p("-u", "统一格式（更易读，常用于补丁）", "Unified format (readable, used for patches)")
        p("-y", "并排对比两列", "Side-by-side comparison")
        p("-r", "递归比较目录", "Recursively compare directories")
        p("-i", "忽略大小写差异", "Ignore case differences")
        p("-w", "忽略空白差异", "Ignore all whitespace")
        e("diff -u old.conf new.conf", "对比两个配置文件", "Compare two config files")
        e("diff -r dirA dirB", "比较两个目录有哪些文件不同", "Compare two directories")
        e("diff -u a.txt b.txt > fix.patch", "生成补丁文件", "Create a patch file")
        rel("cmp", "patch", "comm")
    }

    c("cmp", "逐字节比较文件", "Compare two files byte by byte", "cmp [选项] 文件1 文件2") {
        detail("只关心「是否相同 / 第一个不同在哪」时比 diff 更快。", "Faster than diff when you only care whether two binary files differ.")
        p("-s", "静默模式，只用退出码表示结果", "Silent; report via exit status only")
        p("-l", "列出所有不同的字节位置", "List every differing byte")
        e("cmp -s a.bin b.bin && echo same", "相同则输出 same", "Print 'same' when identical")
        rel("diff", "md5sum")
    }

    c("echo", "输出一行文本", "Print a line of text", "echo [选项] 字符串") {
        detail("最基础的打印命令，脚本里用来提示信息或写入文件。", "The simplest way to print text — used for messages and writing files in scripts.")
        p("-n", "结尾不换行", "Do not output a trailing newline")
        p("-e", "解释转义字符，如 \\n \\t", "Enable interpretation of escapes")
        p("-E", "不解释转义字符（默认）", "Disable escape interpretation (default)")
        e("echo 'Hello, Linux!'", "打印一句话", "Print a message")
        e("echo 'export PATH=\$PATH:/opt/bin' >> ~/.bashrc", "把一行配置追加到文件末尾", "Append a line to a config file")
        e("echo -e 'a\\tb\\tc'", "打印带制表符的内容", "Print text with a tab")
        tip("输出含变量的字符串请用双引号：单引号里 \$VAR 不会被展开。")
        rel("printf", "cat", "export")
    }

    c("printf", "格式化输出", "Format and print data", "printf '格式' 参数...") {
        detail("比 echo 更可控：支持宽度、对齐、进制、转义，跨 shell 行为一致。", "More predictable than echo: width, alignment, bases and escapes behave consistently across shells.")
        p("%s", "字符串占位符", "String placeholder")
        p("%d", "整数占位符", "Decimal integer placeholder")
        p("%.2f", "保留两位小数", "Float with two decimals")
        p("\\n", "换行", "Newline")
        p("\\t", "制表符", "Tab")
        e("printf 'Name: %-10s Age: %d\\n' Alex 18", "左对齐输出表格", "Print an aligned row")
        e("printf '%x\\n' 255", "以十六进制输出 ff", "Print 255 in hex")
        e("printf '%.2f\\n' 3.14159", "输出 3.14", "Print 3.14")
        rel("echo", "awk")
    }

    c("nano", "新手友好的终端编辑器", "A beginner-friendly terminal editor", "nano [选项] 文件") {
        detail("底部直接显示快捷键，改完 Ctrl+O 保存、Ctrl+X 退出，比 vim 好上手。", "Shows its shortcuts at the bottom: Ctrl+O to save, Ctrl+X to quit — much friendlier than vim.")
        p("-w", "关闭自动换行", "Disable automatic line wrapping")
        p("-l", "显示行号", "Show line numbers")
        p("-B", "修改前自动备份", "Back up the previous version")
        e("nano ~/.bashrc", "编辑 bash 配置文件", "Edit your bash config")
        e("sudo nano /etc/hosts", "编辑需要 root 权限的文件", "Edit a root-owned file")
        tip("Ctrl+O 保存，Ctrl+X 退出，Ctrl+W 搜索，Ctrl+K 剪切当前行，Ctrl+U 粘贴。")
        rel("vim", "cat", "sed")
    }

    c("vim", "强大的模式化编辑器", "A powerful modal text editor", "vim [选项] 文件") {
        detail(
            "vim 有普通 / 插入 / 命令三种模式。打开文件默认是普通模式，按 i 进入编辑，Esc 回到普通模式，:w 保存、:q 退出。",
            "vim is modal: you start in Normal mode, press i to insert text, Esc to return, then :w to save and :q to quit."
        )
        p("i", "进入插入模式开始编辑", "Enter insert mode before the cursor")
        p("Esc", "回到普通模式", "Return to normal mode")
        p(":w", "保存文件", "Write (save) the file")
        p(":q / :q!", "退出 / 强制退出不保存", "Quit / quit without saving")
        p(":wq 或 ZZ", "保存并退出", "Save and quit")
        p("dd / yy / p", "删除一行 / 复制一行 / 粘贴", "Delete / yank / paste a line")
        p("/关键词", "搜索内容", "Search for text")
        p("u", "撤销上一步", "Undo")
        e("vim +/error app.log", "打开文件并直接跳到第一个 error", "Open a file at the first 'error'")
        e("vim +100 file.txt", "打开文件并跳到第 100 行", "Open a file at line 100")
        e("vimdiff a.conf b.conf", "并排对比并编辑两个文件", "Compare and edit two files side by side")
        tip("卡在 vim 里出不来？按 Esc，然后输入 :q! 回车即可强制退出。")
        rel("nano", "less", "sed")
    }

    c("vimdiff", "并排对比并编辑文件差异", "Compare files side by side in vim", "vimdiff 文件1 文件2") {
        p("]c / [c", "跳到下一个 / 上一个差异处", "Jump to the next / previous change")
        p("do / dp", "从对方拉取更改 / 把本方更改推给对方", "Obtain / put a diff from the other file")
        p(":qa", "退出所有窗口", "Quit all windows")
        e("vimdiff old.conf new.conf", "对比两个配置文件并合并", "Compare and merge two config files")
        rel("diff", "vim")
    }

    c("md5sum", "计算文件校验值", "Compute and check MD5 checksums", "md5sum [选项] 文件") {
        detail("下载大文件后校验完整性；同类命令还有 sha256sum（更安全）。", "Verify downloads; sha256sum is the stronger modern alternative.")
        p("-c", "根据校验文件验证", "Read checksums from a file and verify")
        p("-b", "二进制模式读取", "Read in binary mode")
        e("md5sum ubuntu.iso", "输出 ISO 的 MD5", "Print the MD5 of an ISO")
        e("sha256sum -c SHA256SUMS", "校验下载文件是否完整", "Verify a downloaded file")
        rel("cmp", "sha256sum", "diff")
    }

    c("sha256sum", "计算 SHA-256 校验值", "Compute and check SHA-256 checksums", "sha256sum [选项] 文件") {
        detail("比 MD5 更可靠的完整性校验方式，官方下载页通常提供该值。", "The standard integrity check today; most projects publish SHA-256 sums.")
        p("-c", "校验文件中的哈希值", "Verify checksums listed in a file")
        e("sha256sum file.zip", "输出文件的 SHA-256", "Print the SHA-256 of a file")
        e("echo 'hello' | sha256sum", "计算字符串的哈希", "Hash a string from stdin")
        rel("md5sum", "cmp")
    }
}
