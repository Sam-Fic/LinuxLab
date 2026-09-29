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

/** Shell 脚本入门 —— 把命令组合成自动化工具 */
val ShellGroup = group("shell", "Shell 脚本入门", "Shell Scripting Basics", "code") {

    c("#!/bin/bash", "脚本的第一行（shebang）", "The shebang line every script starts with", "#!/bin/bash 或 #!/usr/bin/env bash") {
        detail(
            "告诉系统用哪个解释器执行脚本。写完脚本后要 chmod +x 才能像命令一样运行。",
            "Tells the system which interpreter runs the script. Remember chmod +x before running it like a command."
        )
        e("chmod +x hello.sh && ./hello.sh", "赋予执行权限并运行", "Make it executable and run it")
        e("#!/usr/bin/env bash", "更可移植的写法（自动查找 bash）", "More portable: finds bash via PATH")
        e("bash hello.sh", "不赋权限也能运行", "Run it without the executable bit")
        tip("脚本不在 PATH 里时必须用 ./脚本名 执行，直接写 hello.sh 会提示找不到命令。")
        rel("chmod", "bash", "source", "export")
    }

    c("变量 variable", "定义和使用变量", "Declare and use shell variables", "NAME=value  使用：\$NAME") {
        detail(
            "赋值时等号两边不能有空格；使用变量要加 \$。用 \${NAME} 可以明确变量边界。",
            "No spaces around '=' when assigning; prefix with \$ to read it. \${NAME} makes the boundary explicit."
        )
        e("NAME='Alex'; echo \"Hello, \$NAME\"", "定义并输出变量", "Define and print a variable")
        e("FILE=\${DIR:-/tmp}/app.log", "用默认值：DIR 未设置时用 /tmp", "Fall back to a default value")
        e("echo \"\${PATH//:/\\n}\"", "把 PATH 按冒号切成多行", "Split PATH into lines")
        tip("变量名两边加大括号 \$ {VAR} 是良好的习惯，能避免拼接时的歧义。")
        rel("export", "env", "quoting")
    }

    c("引号 quoting", "单引号、双引号与反引号的区别", "Single, double and back quotes", "'...' / \"...\" / \$(...)") {
        detail(
            "单引号原样输出、不做任何替换；双引号会展开变量和命令；命令替换请用 \$(...) 而不是反引号。",
            "Single quotes are literal; double quotes expand variables and commands; prefer \$(...) over backticks for command substitution."
        )
        e("echo '今天 \$USER'", "输出：今天 \$USER（不展开）", "Prints the literal text")
        e("echo \"今天 \$USER\"", "输出：今天 alex（会展开）", "Expands the variable")
        e("echo \"现在时间：\$(date +%T)\"", "在字符串里嵌入命令结果", "Embed a command result")
        tip("文件名可能含空格时，变量一定要加双引号：cp \"\$file\" /tmp")
        rel("变量 variable", "命令替换", "echo")
    }

    c("\$? 退出状态码", "判断上一条命令是否成功", "Check the exit status of the last command", "命令; echo \$?") {
        detail("0 表示成功，1-255 表示各种失败。脚本里靠它做错误处理。", "0 means success, 1-255 indicates failure. Scripts use it for error handling.")
        e("ls /nonexistent; echo \$?", "输出非 0 表示失败", "Prints a non-zero status")
        e("if grep -q 'error' app.log; then echo '发现错误'; fi", "用命令的成败直接做条件", "Use a command as a condition")
        e("command || exit 1", "失败就退出脚本", "Exit the script on failure")
        rel("set -e", "if 条件判断", "exit")
    }

    c("test / [ ]", "条件测试", "Evaluate conditional expressions", "[ 表达式 ] 或 [[ 表达式 ]]", level = Level.ADVANCED) {
        detail("方括号实际上是 test 命令，括号内侧必须有空格。[[ ]] 是 bash 的增强版，更安全。", "Brackets are the test command, so spaces inside are mandatory. [[ ]] is bash's safer, richer form.")
        p("-f / -d / -e", "是文件 / 是目录 / 存在", "File / directory / exists")
        p("-r / -w / -x", "可读 / 可写 / 可执行", "Readable / writable / executable")
        p("-z / -n", "字符串为空 / 非空", "String is empty / not empty")
        p("= / !=", "字符串相等 / 不等", "String equal / not equal")
        p("-eq -ne -lt -gt", "数字比较：等于、不等、小于、大于", "Numeric comparisons")
        e("[ -f /etc/passwd ] && echo '文件存在'", "判断文件是否存在", "Test whether a file exists")
        e("[ \"\$USER\" = 'root' ] || echo '不是 root'", "字符串比较", "Compare strings")
        e("[[ \"\$file\" == *.log ]] && echo '日志文件'", "模式匹配（[[ ]] 专属）", "Pattern matching with [[ ]]")
        tip("数字比较用 -eq，字符串比较用 =，混用是新手最常见的错误。")
        rel("if 条件判断", "\$? 退出状态码", "case 分支")
    }

    c("if 条件判断", "根据条件执行不同分支", "Conditional execution with if", "if 条件; then ... fi") {
        e("if [ -f a.txt ]; then echo '存在'; else echo '不存在'; fi", "最简单的二分支判断", "A simple if/else")
        e("if [ \$? -eq 0 ]; then echo ok; elif [ \$? -eq 1 ]; then echo warn; else echo fail; fi", "多分支判断", "Multiple branches")
        e("if grep -q 'error' log; then echo '有错误'; fi", "直接用命令的结果做判断", "Use a command as the condition")
        tip("then 和 if 写在同一行时要加分号：if [ ... ]; then")
        rel("test / [ ]", "case 分支", "\$? 退出状态码")
    }

    c("case 分支", "多值匹配的清晰写法", "Match a value against several patterns", "case 值 in 模式) ... esac") {
        e("case \$1 in start) echo 启动;; stop) echo 停止;; *) echo '用法: \$0 start|stop';; esac", "典型的启动脚本写法", "A classic start/stop script pattern")
        e("case \$OSTYPE in linux*) echo Linux;; darwin*) echo macOS;; esac", "按系统类型分支", "Branch on the OS type")
        rel("if 条件判断", "函数 function", "getopts")
    }

    c("for 循环", "遍历列表或文件", "Loop over a list of items", "for 变量 in 列表; do ... done") {
        e("for f in *.log; do echo \"处理 \$f\"; done", "遍历当前目录的日志文件", "Loop over log files")
        e("for i in {1..5}; do echo \"第 \$i 次\"; done", "固定次数循环", "A counted loop")
        e("for user in \$(cut -d: -f1 /etc/passwd); do echo \$user; done", "遍历命令输出", "Loop over command output")
        e("for ((i=0; i<10; i++)); do echo \$i; done", "C 风格循环", "A C-style loop")
        tip("文件名含空格时，用 while read 或 find -print0 更安全。")
        rel("while 循环", "find", "xargs")
    }

    c("while 循环", "条件满足时反复执行", "Loop while a condition holds", "while 条件; do ... done") {
        e("while read -r line; do echo \$line; done < data.txt", "逐行读取文件（最常用）", "Read a file line by line")
        e("while true; do ping -c1 host >/dev/null && echo up; sleep 5; done", "每 5 秒探活一次", "Poll a host every five seconds")
        e("tail -f app.log | while read -r l; do echo \"[\$(date +%T)] \$l\"; done", "给实时日志加时间戳", "Add timestamps to a live log")
        tip("逐行读文件用 while read，不要用 for，后者会按空格拆分。")
        rel("for 循环", "read", "until 循环")
    }

    c("until 循环", "条件不满足时反复执行", "Loop until a condition becomes true", "until 条件; do ... done") {
        e("until ping -c1 example.com >/dev/null; do echo '等待联网...'; sleep 2; done", "循环等待网络恢复", "Wait until the network is back")
        e("until [ -f /tmp/ready ]; do sleep 1; done; echo ready", "等待某个文件出现", "Wait for a file to appear")
        rel("while 循环", "sleep", "test / [ ]")
    }

    c("函数 function", "把重复逻辑封装起来", "Bundle reusable logic into functions", "函数名() { 命令; }") {
        e("log() { echo \"[\$(date +%F %T)] \$*\"; }; log '开始备份'", "带时间戳的日志函数", "A timestamped log function")
        e("backup() { tar czf \"\$1.tar.gz\" \"\$1\"; }; backup docs", "定义一个备份函数", "Define a backup function")
        e("die() { echo \"错误: \$1\" >&2; exit 1; }", "统一的报错退出函数", "A standard error-and-exit helper")
        tip("函数里用 local 声明局部变量，避免污染全局：local tmp=/tmp/x")
        rel("位置参数", "case 分支", "变量 variable")
    }

    c("位置参数", "获取脚本传入的参数", "Access arguments passed to a script", "\$0 \$1 \$# \$@ \$*") {
        detail("\$0 脚本名，\$1…\$9 第 1…9 个参数，\$# 参数个数，\$@ 全部参数，\$* 全部参数（合并成一个串）。", "\$0 is the script name, \$1…\$9 the arguments, \$# the count, \$@ all arguments, \$* all arguments as one string.")
        e("./deploy.sh prod v1.2   # \$1=prod \$2=v1.2 \$#=2", "传参示例", "Passing arguments")
        e("[ \$# -eq 0 ] && { echo '用法: \$0 环境'; exit 1; }", "检查是否提供了参数", "Verify arguments were given")
        e("for arg in \"\$@\"; do echo \$arg; done", "遍历所有参数（保留空格）", "Iterate over arguments safely")
        tip("总是用 \"\$@\" 而不是 \$@，前者能正确处理含空格的参数。")
        rel("shift", "getopts", "函数 function")
    }

    c("管道 pipe", "把上一条的输出交给下一条", "Chain commands with pipes", "命令1 | 命令2 | 命令3") {
        detail("管道是 Unix 哲学的核心：每个工具只做一件事，用 | 组合出复杂功能。", "The core of the Unix philosophy: each tool does one thing and | composes them.")
        e("cat access.log | awk '{print \$1}' | sort | uniq -c | sort -nr | head", "统计访问最多的 IP（经典管道链）", "Count the top visitor IPs")
        e("ps aux | grep nginx | grep -v grep | awk '{print \$2}'", "取出 nginx 的进程号", "Get the nginx PIDs")
        e("dmesg | tail -20 | grep -i error", "从内核日志尾部找错误", "Find errors at the end of the kernel log")
        tip("管道默认只传递标准输出，错误输出用 2>&1 合并后再传。")
        rel("重定向", "xargs", "tee", "grep")
    }

    c("重定向 redirect", "控制输入输出去哪里", "Redirect input and output streams", "命令 > 文件 2>&1 < 文件") {
        detail(
            "0 标准输入、1 标准输出、2 标准错误。> 覆盖写，>> 追加写，2>&1 把错误并入标准输出。",
            "0 stdin, 1 stdout, 2 stderr. '>' overwrites, '>>' appends, and 2>&1 merges stderr into stdout."
        )
        e("ls > out.txt 2> err.txt", "正确输出和错误分开保存", "Save output and errors separately")
        e("command > /dev/null 2>&1", "丢弃全部输出（静默执行）", "Discard all output")
        e("echo 'hello' >> notes.txt", "追加写入文件", "Append to a file")
        e("cat < input.txt > output.txt", "从文件读入、写到文件", "Read from and write to files")
        e("command 2>&1 | tee run.log", "既看结果又保存日志", "Show output and save it too")
        tip("> file 会先清空文件；确认无误前可用 >> 或先备份。")
        rel("管道 pipe", "tee", "echo")
    }

    c("命令替换", "把命令结果当作值使用", "Substitute the output of a command", "\$(命令) 或 旧写法 '命令'") {
        e("TODAY=\$(date +%F)", "把日期存入变量", "Store today's date in a variable")
        e("cp file.txt \"file.\$(date +%s).bak\"", "用时间戳生成文件名", "Build a filename with a timestamp")
        e("echo \"共有 \$(ls | wc -l) 个文件\"", "在字符串里嵌入结果", "Embed a result in a string")
        tip("始终用 \$(...) 而不是反引号：可嵌套、可读性更好。")
        rel("引号 quoting", "变量 variable", "xargs")
    }

    c("read", "读取用户输入", "Read a line from standard input", "read [选项] 变量") {
        p("-p", "显示提示语", "Output a prompt before reading")
        p("-s", "隐藏输入（用于密码）", "Silent mode, for passwords")
        p("-r", "不把反斜杠当转义符（推荐）", "Do not treat backslash specially")
        e("read -p '请输入用户名: ' name; echo \"你好, \$name\"", "交互式获取输入", "Prompt for a value")
        e("read -rsp '密码: ' pw; echo", "静默读取密码", "Read a password silently")
        e("while read -r line; do echo \$line; done < file.txt", "逐行读取文件", "Read a file line by line")
        rel("while 循环", "echo", "位置参数")
    }

    c("shift", "移动位置参数", "Shift positional parameters to the left", "shift [N]") {
        detail("常用于「先取出第一个参数，剩下的交给循环」的场景。", "Handy when you consume the first argument and loop over the rest.")
        e("name=\$1; shift; echo \"剩余参数: \$@\"", "取出第一个参数后 shift", "Take the first argument, then shift")
        e("while [ \$# -gt 0 ]; do echo \$1; shift; done", "逐个处理所有参数", "Process every argument")
        rel("位置参数", "getopts", "while 循环")
    }

    c("getopts", "解析命令行选项（如 -f x）", "Parse short command-line options", "while getopts 'hf:' opt; do ... done", level = Level.ADVANCED) {
        detail("选项字符串里带冒号表示该选项需要参数，如 'hf:' 表示 -f 需要一个值。", "A colon after a letter means that option takes a value: 'hf:' means -f needs an argument.")
        e("while getopts 'hf:' o; do case \$o in h) usage;; f) FILE=\$OPTARG;; esac; done", "标准选项解析模板", "A standard option-parsing loop")
        e("./script.sh -f data.txt -v", "调用示例", "Example invocation")
        rel("位置参数", "case 分支", "shift")
    }

    c("trap", "捕获信号做清理工作", "Run commands when signals arrive", "trap '命令' 信号", level = Level.ADVANCED) {
        detail("脚本被 Ctrl+C 或退出时执行清理，比如删除临时文件。", "Run cleanup when the script is interrupted or exits, e.g. deleting temp files.")
        e("trap 'rm -f /tmp/mytmp.\$\$' EXIT", "脚本退出时自动清理临时文件", "Clean up a temp file on exit")
        e("trap 'echo 收到中断; exit' INT", "捕获 Ctrl+C", "Catch Ctrl+C")
        e("trap '' INT", "忽略中断信号（不推荐滥用）", "Ignore interrupts (use sparingly)")
        rel("exit", "kill", "函数 function")
    }

    c("source / .", "在当前 shell 中执行脚本", "Run a script in the current shell", "source 脚本 或 . 脚本") {
        detail("与直接执行不同：source 不会开子 shell，脚本里 export 的变量会留在当前终端。", "Unlike executing, source does not spawn a subshell, so exported variables stay in your shell.")
        e("source ~/.bashrc", "让配置修改立即生效", "Apply config changes immediately")
        e("source .venv/bin/activate", "激活 Python 虚拟环境", "Activate a virtual environment")
        e(". ./env.sh", "点号是 source 的简写", "A dot is shorthand for source")
        rel("export", "bash", "#!/bin/bash")
    }

    c("set -euo pipefail", "让脚本更安全（强烈推荐）", "Make scripts fail fast and safely", "set -euo pipefail", level = Level.ADVANCED) {
        detail(
            "-e 出错即退出；-u 使用未定义变量时报错；-o pipefail 管道中任一环节失败即失败。专业脚本的标配。",
            "-e exits on the first error; -u errors on undefined variables; -o pipefail makes a pipeline fail if any stage fails. The professional default."
        )
        e("set -euo pipefail", "放在 shebang 之后的第二行", "Place it right after the shebang")
        e("set -x", "调试模式：打印每条执行的命令", "Debug mode: trace every command")
        e("set -e; cp a b; rm c", "任一步失败都立刻停止", "Stop immediately if any step fails")
        tip("调试时临时用 bash -x 脚本名 运行，可以看到每一步展开后的命令。")
        rel("\$? 退出状态码", "exit", "trap")
    }

    c("算术运算", "在 shell 里做数学计算", "Perform integer arithmetic", "\$((表达式))") {
        e("echo \$((3 + 5 * 2))", "输出 13（支持优先级）", "Prints 13, honouring precedence")
        e("count=\$((count + 1))", "计数器自增", "Increment a counter")
        e("echo \$((1024 * 1024))", "计算字节数", "Compute a byte count")
        e("echo 'scale=2; 10/3' | bc", "小数运算需要用 bc", "Use bc for floating point")
        tip("bash 原生只支持整数，小数请用 bc 或 awk。")
        rel("变量 variable", "for 循环", "awk")
    }

    c("数组 array", "批量存放一组值", "Store multiple values in one variable", "arr=(a b c)", level = Level.ADVANCED) {
        e("files=(*.log); echo \"\${files[0]}\"", "把文件列表存成数组", "Store a file list in an array")
        e("arr=(a b c); for x in \"\${arr[@]}\"; do echo \$x; done", "遍历数组", "Iterate over an array")
        e("echo \"\${#arr[@]}\"", "获取数组长度", "Print the array length")
        tip("遍历数组要用 \"\${arr[@]}\" 加双引号，才能保留元素中的空格。")
        rel("for 循环", "变量 variable", "while 循环")
    }

    c("&& 与 ||", "按成败串联命令", "Chain commands by success or failure", "命令1 && 命令2 || 命令3") {
        detail("&& 前面成功才执行后面；|| 前面失败才执行后面。比 if 更简洁。", "&& runs the next command only on success; || only on failure. Shorter than an if block.")
        e("mkdir build && cd build && cmake ..", "每步都成功才继续", "Proceed only if each step succeeds")
        e("ping -c1 host >/dev/null && echo 在线 || echo 离线", "简单的三元判断", "A compact ternary check")
        e("command || { echo 失败; exit 1; }", "失败时执行多条语句", "Run several statements on failure")
        rel("if 条件判断", "\$? 退出状态码", "test / [ ]")
    }

    c("调试脚本 bash -x", "一步步看清脚本在做什么", "Trace script execution", "bash -x 脚本名") {
        detail("每一行会先打印展开后的命令（以 + 开头），定位逻辑错误最有效。", "Each line is printed expanded (prefixed with +) before running — the fastest way to find logic bugs.")
        e("bash -x deploy.sh", "完整跟踪脚本执行", "Trace the whole script")
        e("set -x  # 只在某一段开启调试\n...\nset +x", "局部开启调试", "Enable tracing for part of the script")
        e("bash -n script.sh", "只检查语法错误，不执行", "Check syntax without running")
        e("shellcheck script.sh", "静态检查（强烈推荐安装）", "Static analysis — install shellcheck")
        tip("写完脚本先跑一遍 shellcheck，能避掉 80% 的隐藏问题。")
        rel("set -euo pipefail", "echo", "exit")
    }
}
