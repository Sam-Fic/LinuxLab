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

/** 系统信息与环境 —— 认识你的机器，配置你的 shell */
val SystemInfoGroup = group("system", "系统信息与环境变量", "System Info & Environment", "computer") {

    c("uname", "查看系统与内核信息", "Print system information", "uname [选项]") {
        p("-a", "显示全部信息（最常用）", "Print all information")
        p("-s", "内核名称（Linux）", "Kernel name")
        p("-r", "内核版本", "Kernel release")
        p("-m", "硬件架构，如 x86_64 / aarch64", "Machine hardware name")
        p("-o", "操作系统名称", "Operating system")
        e("uname -a", "一次查看内核、主机名、架构等全部信息", "Show everything at once")
        e("uname -m", "确认 CPU 架构（下载软件包前必看）", "Check the CPU architecture")
        tip("要看发行版名称请用 cat /etc/os-release。")
        rel("hostname", "lscpu", "lsb_release")
    }

    c("hostname", "查看或设置主机名", "Show or set the system hostname", "hostname [选项]") {
        e("hostname", "显示当前主机名", "Print the hostname")
        e("sudo hostnamectl set-hostname web01", "永久修改主机名", "Change the hostname permanently")
        rel("uname", "hostnamectl", "who")
    }

    c("uptime", "查看运行时长与负载", "Show how long the system has been running", "uptime [选项]") {
        detail("输出当前时间、已运行时长、在线用户数和 1/5/15 分钟平均负载。", "Shows the current time, uptime, logged-in users and the 1/5/15-minute load averages.")
        p("-p", "只显示运行时长（人类可读）", "Show uptime in a pretty format")
        p("-s", "显示开机时间", "Show the boot time")
        e("uptime", "查看运行了多久和负载", "Show uptime and load averages")
        e("uptime -s", "查看上次开机时刻", "Show when the system booted")
        tip("负载均值接近或超过 CPU 核心数，说明系统已经饱和。")
        rel("w", "top", "free")
    }

    c("free", "查看内存使用情况", "Display memory usage", "free [选项]") {
        detail("注意 available 列：它才是「还能用多少内存」，buff/cache 是内核用来加速的缓存。", "Watch the 'available' column — buffer/cache memory is reclaimable, not wasted.")
        p("-h", "以 KB/MB/GB 显示（推荐）", "Human-readable units")
        p("-m", "以 MB 显示", "Show values in megabytes")
        p("-s N", "每 N 秒刷新一次", "Refresh every N seconds")
        p("-t", "显示总计行", "Show a total line")
        e("free -h", "查看内存和交换分区", "Show memory and swap usage")
        e("free -h -s 3", "每 3 秒刷新内存情况", "Watch memory every 3 seconds")
        rel("top", "vmstat", "swapon", "df")
    }

    c("vmstat", "查看虚拟内存与系统整体状态", "Report virtual memory statistics", "vmstat [间隔] [次数]", level = Level.ADVANCED) {
        detail("一次输出进程、内存、交换、IO、系统和 CPU 六类指标，适合快速定位瓶颈。", "Reports processes, memory, swap, I/O, system and CPU in one line — great for a quick bottleneck check.")
        p("-w", "加宽输出，列对齐更易读", "Wide output, easier to read")
        p("-S m", "以 MB 为单位显示", "Show values in megabytes")
        e("vmstat 1 5", "每秒采样一次，共 5 次", "Sample once per second, five times")
        e("vmstat -w -S m", "以 MB 单位显示一次整体状态", "Show a wide summary in MB")
        tip("si/so 长期不为 0 表示内存不足开始换页；wa 高表示 CPU 在等磁盘。")
        rel("free", "top", "iostat")
    }

    c("lscpu", "查看 CPU 信息", "Display information about the CPU", "lscpu [选项]") {
        e("lscpu", "查看核心数、型号、缓存等", "Show cores, model name and caches")
        e("nproc", "只输出 CPU 逻辑核心数", "Print the number of processing units")
        rel("uname", "free", "top")
    }

    c("lsblk", "查看磁盘与分区结构", "List block devices", "lsblk [选项]") {
        p("-f", "显示文件系统类型和 UUID", "Show filesystem type and UUID")
        p("-o", "自定义输出列", "Choose output columns")
        e("lsblk", "列出所有磁盘及分区", "List all disks and partitions")
        e("lsblk -f", "查看分区的文件系统和挂载点", "Show filesystems and mount points")
        rel("df", "mount", "fdisk", "blkid")
    }

    c("lshw", "查看详细硬件清单", "List detailed hardware configuration", "lshw [选项]", level = Level.ADVANCED) {
        p("-short", "简洁列表", "Compact listing")
        p("-class 类型", "只看某类硬件，如 disk / network", "Show only one class, e.g. disk")
        e("sudo lshw -short", "列出全部硬件概要", "Show a hardware summary")
        e("sudo lshw -class disk", "只查看磁盘信息", "Show disk devices only")
        rel("lscpu", "lsblk", "dmidecode")
    }

    c("dmesg", "查看内核日志", "Print kernel ring buffer messages", "dmesg [选项]") {
        detail("开机信息、驱动加载、硬件错误都在这里；插入 U 盘后立刻能看到识别记录。", "Boot messages, driver loading and hardware errors live here — plug in a USB drive and watch it appear.")
        p("-T", "显示人类可读的时间戳", "Show human-readable timestamps")
        p("-w", "实时跟踪新日志", "Wait for new messages")
        p("-l 级别", "只显示某级别（err / warn）", "Filter by level (err, warn)")
        e("dmesg -T | tail -20", "查看最近的内核消息", "Show the newest kernel messages")
        e("dmesg -Tw", "实时监控内核日志", "Follow kernel messages live")
        e("dmesg | grep -i usb", "查找 USB 设备相关信息", "Look for USB messages")
        rel("journalctl", "tail", "uname")
    }

    c("date", "查看或设置系统时间", "Print or set the system date and time", "date [选项] [+格式]") {
        p("+%F", "输出 2024-09-01 形式日期", "Print an ISO date")
        p("+%T", "输出 时:分:秒", "Print the time")
        p("-d", "解析描述性时间，如 'yesterday'", "Parse a date string")
        e("date '+%Y-%m-%d %H:%M:%S'", "按指定格式输出时间", "Print the date in a custom format")
        e("date -d 'next monday' +%F", "计算下周一的日期", "Compute next Monday's date")
        e("date +%s", "输出 Unix 时间戳", "Print the Unix timestamp")
        tip("脚本里生成日志文件名：backup_\$(date +%F).tar.gz")
        rel("timedatectl", "cal", "uptime")
    }

    c("cal", "显示日历", "Display a calendar", "cal [选项] [[月] 年]") {
        p("-y", "显示整年日历", "Show the whole year")
        p("-3", "显示上一个月、本月、下一个月", "Show previous, current and next month")
        e("cal", "显示本月日历", "Show this month")
        e("cal -y 2024", "显示 2024 年全年日历", "Show all of 2024")
        rel("date", "watch")
    }

    c("env", "查看环境变量", "Run a program in a modified environment", "env [选项] [命令]") {
        detail("不带参数时列出当前所有环境变量；也可以临时指定变量运行程序。", "With no arguments it lists all environment variables; it can also run a command with a modified environment.")
        p("-i", "清空环境后运行命令", "Start with an empty environment")
        p("-u 变量", "删除某个环境变量", "Unset a variable")
        e("env | grep PATH", "查看 PATH 变量", "Inspect the PATH variable")
        e("env LANG=C sort data.txt", "以 C 语言环境排序（结果稳定）", "Sort with a stable locale")
        rel("export", "printenv", "set")
    }

    c("export", "设置环境变量", "Set environment variables for child processes", "export 变量名=值") {
        detail("让变量对当前 shell 及其子进程生效；要永久生效需写进 ~/.bashrc 或 ~/.profile。", "Exports a variable to the current shell and its children; put it in ~/.bashrc for a permanent effect.")
        p("-p", "列出所有已导出的变量", "List all exported variables")
        p("-n", "取消导出", "Remove the export property")
        e("export PATH=\$PATH:/opt/bin", "把 /opt/bin 加入命令搜索路径", "Add /opt/bin to the command search path")
        e("export EDITOR=vim", "设置默认编辑器", "Set your default editor")
        e("echo 'export PATH=\$PATH:/opt/bin' >> ~/.bashrc", "永久生效", "Make it permanent")
        tip("改完 ~/.bashrc 后执行 source ~/.bashrc 立即生效。")
        rel("env", "source", "alias", "echo")
    }

    c("history", "查看命令历史", "Show the command history", "history [选项] [N]") {
        detail("历史记录在 ~/.bash_history；!! 重复上一条，!N 执行第 N 条。", "History is stored in ~/.bash_history; !! repeats the last command and !N runs number N.")
        p("-c", "清空历史记录", "Clear the history list")
        p("-d N", "删除第 N 条记录", "Delete history entry N")
        p("!N", "重新执行第 N 条命令", "Re-run command number N")
        p("Ctrl+R", "反向搜索历史命令（最实用）", "Reverse search through history")
        e("history | grep ssh", "查找曾经用过的 ssh 命令", "Find ssh commands you used before")
        e("!!", "重新执行上一条命令", "Repeat the last command")
        e("history | awk '{print \$2}' | sort | uniq -c | sort -nr | head", "统计最常用的命令", "Show your most used commands")
        tip("Ctrl+R 后输入关键字即可回溯历史命令，是终端里最值得背的快捷键。")
        rel("export", "alias", "fc")
    }

    c("which", "查找命令的可执行文件位置", "Locate a command in PATH", "which [选项] 命令") {
        e("which python3", "输出 /usr/bin/python3", "Show where python3 lives")
        e("which -a python", "列出所有同名可执行文件", "List every python in PATH")
        tip("which 只查 PATH 里的可执行文件，查文件请用 find 或 locate。")
        rel("whereis", "type", "find")
    }

    c("whereis", "查找命令的二进制、源码和手册", "Locate binary, source and manual files", "whereis [选项] 命令") {
        e("whereis ls", "输出 ls 的二进制与手册路径", "Show the ls binary and man pages")
        e("whereis -b python3", "只查找二进制文件", "Search binaries only")
        rel("which", "man", "find")
    }

    c("type", "判断命令的类型", "Show how a command name is interpreted", "type [选项] 命令") {
        detail("区分是 shell 内建命令（如 cd）、外部程序（如 ls）、别名还是函数。", "Tells you whether a name is a builtin, an external program, an alias or a function.")
        p("-a", "列出所有匹配", "Show all matches")
        p("-t", "只输出类型关键字", "Print a single word describing the type")
        e("type cd", "输出 cd is a shell builtin", "Shows that cd is a builtin")
        e("type -a ls", "显示 ls 的所有来源（可能含别名）", "Show every definition of ls")
        rel("which", "alias", "help")
    }

    c("man", "查看命令手册（最重要）", "Display the manual page for a command", "man [章节] 命令") {
        detail("遇到不认识的命令先 man 一下。按 / 搜索，n 下一个，q 退出。", "When in doubt, read the manual. Press / to search, n for the next hit, q to quit.")
        p("-k 关键字", "按关键字搜索手册（等同 apropos）", "Search manual pages for a keyword")
        p("-f 命令", "显示命令的一句话简介（等同 whatis）", "Show a one-line description")
        p("1-9", "手册章节：1 命令、5 配置文件、8 管理命令", "Sections: 1 commands, 5 files, 8 admin")
        e("man ls", "查看 ls 的完整手册", "Read the ls manual page")
        e("man 5 passwd", "查看 /etc/passwd 文件格式说明", "Read the passwd file format")
        e("man -k copy", "搜索所有和复制相关的手册", "Search for copy-related pages")
        tip("记不住参数就看手册里的 SYNOPSIS 和 EXAMPLES 部分，最快。")
        rel("help", "info", "whatis", "type")
    }

    c("help", "查看 shell 内建命令帮助", "Get help for shell builtins", "help [选项] [命令]") {
        detail("cd、echo、export 这类内建命令没有独立手册页，要用 help。", "Builtins such as cd, echo and export have no man page — use help instead.")
        p("-m", "以类 man 的格式输出", "Display usage in pseudo-manpage format")
        e("help cd", "查看 cd 的用法", "Show help for cd")
        e("help | head", "列出所有内建命令", "List all builtins")
        rel("man", "type", "info")
    }

    c("alias", "给命令起短别名", "Create command shortcuts", "alias 名称='命令'") {
        detail("把长命令变成好记的短命令；写进 ~/.bashrc 才能永久生效。", "Turn long commands into short ones; add them to ~/.bashrc to keep them.")
        p("-p", "列出所有别名", "List all aliases")
        e("alias ll='ls -alh'", "用 ll 代替长格式列表", "Shorten the long listing command")
        e("alias gs='git status'", "git 常用命令简写", "Shortcut for git status")
        e("unalias ll", "删除别名", "Remove an alias")
        tip("临时绕过别名：在命令前加反斜杠，如 \\ls。")
        rel("unalias", "export", "history", "type")
    }

    c("unalias", "删除命令别名", "Remove an alias", "unalias [选项] 名称") {
        p("-a", "删除所有别名", "Remove all aliases")
        e("unalias ll", "删除 ll 别名", "Remove the ll alias")
        rel("alias", "type")
    }

    c("watch", "周期性重复执行命令", "Run a command repeatedly and show the output", "watch [选项] 命令") {
        detail("默认每 2 秒执行一次，全屏显示结果，适合观察变化趋势。", "Runs the command every 2 seconds by default and shows the result full-screen.")
        p("-n N", "每 N 秒执行一次", "Set the interval to N seconds")
        p("-d", "高亮显示和上一次的差异", "Highlight changes between updates")
        p("-t", "不显示标题栏", "Turn off the header")
        e("watch -n 1 free -h", "每秒查看内存变化", "Watch memory every second")
        e("watch -d 'ls -l | wc -l'", "高亮文件数量的变化", "Highlight changes in the file count")
        e("watch -n 5 nvidia-smi", "监控 GPU 使用情况", "Monitor GPU usage")
        rel("tail", "top", "sleep")
    }

    c("clear", "清屏", "Clear the terminal screen", "clear") {
        e("clear", "清空屏幕（Ctrl+L 同样效果）", "Clear the screen (Ctrl+L does the same)")
        rel("reset", "history")
    }

    c("sleep", "等待一段时间", "Pause for a specified amount of time", "sleep 时长[后缀]") {
        detail("后缀 s 秒（默认）、m 分、h 时、d 天，脚本里常用于节流或重试间隔。", "Suffixes: s seconds (default), m minutes, h hours, d days — used for pacing scripts.")
        e("sleep 5", "等待 5 秒", "Wait five seconds")
        e("sleep 2m && echo done", "2 分钟后提示", "Print done after two minutes")
        rel("watch", "timeout", "at")
    }

    c("exit", "退出当前 shell", "Exit the shell", "exit [状态码]") {
        detail("0 表示成功，非 0 表示异常；脚本里用 exit 1 表示失败。", "Status 0 means success, non-zero means failure — scripts use exit 1 to signal an error.")
        e("exit", "关闭当前终端会话", "Close the current shell")
        e("exit 1", "以失败状态退出（脚本里常用）", "Exit with a failure status")
        rel("logout", "echo")
    }

    c("reset", "重置终端显示", "Reset the terminal to a sane state", "reset") {
        detail("当终端因为输出二进制内容而乱码时，执行 reset 恢复。", "Use it when a terminal gets garbled after printing binary data.")
        e("reset", "恢复终端到正常状态", "Restore the terminal")
        rel("clear", "stty")
    }

    c("lsb_release", "查看发行版信息", "Show distribution-specific information", "lsb_release [选项]") {
        p("-a", "显示全部信息", "Show all information")
        p("-d", "只显示描述（发行版名称）", "Show the description only")
        e("lsb_release -a", "查看发行版代号和版本号", "Show the distribution release info")
        e("cat /etc/os-release", "另一种通用查看方式", "A portable alternative")
        rel("uname", "hostnamectl")
    }

    c("locale", "查看语言与编码设置", "Show the current locale settings", "locale [选项]") {
        p("-a", "列出系统已生成的全部语言环境", "List all available locales")
        e("locale", "查看当前语言环境（LANG、LC_* 等）", "Show current locale variables")
        e("export LC_ALL=C", "临时切到 C 语言环境，命令输出更稳定", "Use the C locale for stable output")
        rel("env", "export", "timedatectl")
    }
}
