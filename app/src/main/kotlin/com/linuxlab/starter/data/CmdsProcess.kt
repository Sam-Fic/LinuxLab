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

/** 进程与作业 —— 让程序在后台、前台、定时地正确运行 */
val ProcessGroup = group("process", "进程与作业", "Processes & Jobs", "memory") {

    c("ps", "查看当前进程", "Report a snapshot of running processes", "ps [选项]") {
        detail("最常用组合是 ps aux（BSD 风格）或 ps -ef（System V 风格）。", "The two common forms are 'ps aux' (BSD style) and 'ps -ef' (System V style).")
        p("aux", "显示所有用户的所有进程，含 CPU / 内存占用", "Show every process with CPU and memory usage")
        p("-ef", "完整格式显示所有进程", "Full-format listing of all processes")
        p("-u 用户", "只看某个用户的进程", "Show processes of a given user")
        p("--sort", "按列排序，如 --sort=-%mem", "Sort by a column, e.g. --sort=-%mem")
        p("-o", "自定义输出列", "Choose which columns to print")
        e("ps aux | grep nginx", "查找 nginx 相关进程", "Find nginx processes")
        e("ps aux --sort=-%mem | head", "找出占用内存最多的进程", "Show the top memory hogs")
        e("ps -eo pid,ppid,cmd", "只看进程号、父进程号和命令", "Show only PID, PPID and command")
        tip("ps aux 里的 STAT 列：R 运行中、S 休眠、Z 僵尸、D 不可中断（通常等 IO）。")
        rel("top", "pgrep", "kill")
    }

    c("top", "实时监控系统进程", "Display Linux processes interactively", "top [选项]", level = Level.ADVANCED) {
        detail("动态刷新进程列表和资源占用，类似 Windows 的任务管理器。", "A live, refreshing view of processes and resource usage — the terminal task manager.")
        p("-u 用户", "只看某个用户的进程", "Show only one user's processes")
        p("-p PID", "只监控指定进程", "Monitor specific PIDs")
        p("-d N", "刷新间隔 N 秒", "Set the refresh delay")
        p("q", "退出", "Quit")
        p("P / M", "按 CPU / 内存排序（运行中的快捷键）", "Sort by CPU / memory (interactive keys)")
        p("k", "杀死进程（会询问 PID）", "Kill a process (prompts for a PID)")
        e("top", "打开实时进程监控", "Start the interactive process view")
        e("top -u www-data", "只看 web 服务用户的进程", "Watch processes of one user")
        e("top -d 5 -p 1234", "每 5 秒刷新一次指定进程", "Refresh a single process every 5s")
        tip("顶部三行是负载均值（1/5/15 分钟）；大于 CPU 核心数表示系统繁忙。")
        rel("htop", "ps", "free", "uptime")
    }

    c("htop", "更好看的进程监控器", "An interactive process viewer", "htop [选项]", level = Level.ADVANCED) {
        detail("top 的增强版：支持鼠标、彩色显示、横向滚动、直接 F9 杀进程。需自行安装。", "A friendlier top: mouse support, colours, horizontal scrolling and F9 to kill. Needs installing.")
        p("-u", "只显示指定用户的进程", "Show only one user's processes")
        p("F4", "按关键字过滤", "Filter by keyword (interactive)")
        p("F6", "选择排序字段", "Choose the sort column (interactive)")
        p("F9", "向选中进程发送信号", "Send a signal to the selected process")
        e("htop", "启动交互式监控", "Launch htop")
        e("sudo apt install htop", "安装 htop", "Install htop")
        rel("top", "ps", "kill")
    }

    c("kill", "向进程发送信号（结束进程）", "Send a signal to a process", "kill [选项] PID") {
        detail("默认发 TERM(15) 让进程优雅退出；不听话才用 KILL(9) 强制杀掉。", "By default it sends TERM (15) for a graceful exit; use KILL (9) only when a process refuses to die.")
        p("-15 / TERM", "优雅终止（默认）", "Polite termination (default)")
        p("-9 / KILL", "强制杀死，进程无法拦截", "Force kill, cannot be caught")
        p("-1 / HUP", "挂起信号，常用来让服务重载配置", "Hang up, often used to reload configs")
        p("-l", "列出所有可用信号", "List all signal names")
        e("kill 1234", "请求 PID 1234 退出", "Ask PID 1234 to exit")
        e("kill -9 1234", "强制杀死进程", "Force kill a process")
        e("kill -HUP \$(cat /run/nginx.pid)", "让 nginx 重新加载配置", "Tell nginx to reload its config")
        tip("kill -9 是最后手段：进程来不及保存数据，可能造成文件损坏。")
        rel("pkill", "killall", "pgrep", "ps")
    }

    c("killall", "按进程名结束进程", "Kill processes by name", "killall [选项] 进程名") {
        p("-9", "强制杀死", "Force kill")
        p("-u 用户", "只结束某用户的进程", "Only kill processes of a user")
        p("-i", "逐个确认", "Ask before each kill")
        e("killall -9 chrome", "强制结束所有 chrome 进程", "Force kill every chrome process")
        e("killall -u alex python3", "结束 alex 的所有 python3 进程", "Kill alex's python3 processes")
        rel("pkill", "kill", "pgrep")
    }

    c("pkill", "按名字或条件杀进程", "Signal processes based on name and other attributes", "pkill [选项] 模式", level = Level.ADVANCED) {
        detail("支持模式匹配，比 killall 更灵活；可按终端、用户、启动时间等筛选。", "Matches by pattern and can filter by terminal, user or start time — more flexible than killall.")
        p("-f", "匹配完整命令行而不只是进程名", "Match the full command line")
        p("-u 用户", "只匹配指定用户的进程", "Match processes owned by a user")
        p("-9", "强制杀死", "Force kill")
        p("-n / -o", "只杀最新 / 最旧的匹配进程", "Kill the newest / oldest match")
        e("pkill -f 'python app.py'", "结束命令行里含 python app.py 的进程", "Kill processes matching the command line")
        e("pkill -u alex -f node", "结束 alex 的 node 进程", "Kill alex's node processes")
        tip("pkill -f 会匹配整个命令行，先用 pgrep -af 确认会命中哪些进程。")
        rel("pgrep", "kill", "killall")
    }

    c("pgrep", "按名字查找进程 PID", "Find processes by name and attributes", "pgrep [选项] 模式", level = Level.ADVANCED) {
        p("-a", "同时显示完整命令行", "List the full command line too")
        p("-f", "匹配完整命令行", "Match against the full command line")
        p("-u 用户", "只看指定用户的进程", "Match processes of a user")
        p("-l", "显示进程名", "Show the process name")
        e("pgrep -a nginx", "查看 nginx 进程及其 PID", "Show nginx PIDs with command lines")
        e("pgrep -u alex sshd", "查找 alex 的 sshd 进程", "Find alex's sshd processes")
        rel("pkill", "ps", "kill")
    }

    c("pstree", "以树状结构显示进程", "Display a tree of processes", "pstree [选项] [PID或用户]", level = Level.ADVANCED) {
        p("-p", "显示 PID", "Show PIDs")
        p("-u", "显示进程所属用户", "Show user transitions")
        p("-a", "显示完整命令行", "Show command-line arguments")
        e("pstree -p", "查看进程的父子关系", "Show the parent-child process tree")
        e("pstree -p alex", "只显示 alex 的进程树", "Show alex's process tree")
        rel("ps", "top")
    }

    c("jobs", "查看当前 shell 的后台任务", "List active jobs in the current shell", "jobs [选项]") {
        detail("配合 & 、Ctrl+Z、fg、bg 使用，管理本终端里启动的作业。", "Used together with &, Ctrl+Z, fg and bg to manage jobs started from this shell.")
        p("-l", "同时显示 PID", "List PIDs as well")
        p("-r", "只显示运行中的作业", "Show only running jobs")
        p("-s", "只显示暂停的作业", "Show only stopped jobs")
        e("jobs -l", "列出后台任务及进程号", "List background jobs with PIDs")
        e("sleep 300 &", "在后台运行一条命令", "Run a command in the background")
        tip("%1 表示第 1 号作业，fg %1 可把它调回前台。")
        rel("fg", "bg", "nohup")
    }

    c("fg", "把后台任务调回前台", "Bring a job to the foreground", "fg [%作业号]") {
        e("fg", "恢复最近一个后台任务", "Resume the most recent job")
        e("fg %2", "恢复 2 号作业", "Resume job number 2")
        rel("bg", "jobs", "nohup")
    }

    c("bg", "让暂停的任务在后台继续跑", "Resume a suspended job in the background", "bg [%作业号]") {
        e("bg %1", "让被 Ctrl+Z 暂停的 1 号作业在后台继续", "Continue job 1 in the background")
        tip("流程：命令执行中按 Ctrl+Z 暂停 → bg 转后台 → disown 让它在终端关闭后存活。")
        rel("fg", "jobs", "disown")
    }

    c("nohup", "让进程在注销后继续运行", "Run a command immune to hangups", "nohup 命令 [参数] &") {
        detail("忽略挂断信号，常用于远程 SSH 里启动长时间任务；输出默认写入 nohup.out。", "Ignores the hangup signal — perfect for long jobs over SSH. Output goes to nohup.out by default.")
        e("nohup python3 train.py > train.log 2>&1 &", "后台训练模型并把输出写入日志", "Run a training job in the background")
        e("nohup ./server &", "启动服务，关闭终端也不中断", "Start a server that survives logout")
        tip("更好的长期方案是用 systemd service 或 tmux/screen 会话。")
        rel("disown", "bg", "systemctl")
    }

    c("disown", "把作业从 shell 作业表中移除", "Remove jobs from the shell's job table", "disown [选项] [%作业号]") {
        p("-h", "只标记，不从表中移除（仍可 jobs 看到）", "Mark the job so SIGHUP is not sent")
        p("-a", "对所有作业生效", "Apply to all jobs")
        e("disown -h %1", "让 1 号作业在终端关闭后继续运行", "Keep job 1 alive after logout")
        e("disown -a", "移除所有作业记录", "Remove all jobs from the table")
        rel("nohup", "bg", "jobs")
    }

    c("nice", "以指定优先级启动进程", "Run a program with modified scheduling priority", "nice [选项] 命令") {
        detail("nice 值范围 -20（最高优先）到 19（最低优先），默认是 0。普通用户只能调低优先级。", "Niceness runs from -20 (highest priority) to 19 (lowest); default is 0. Regular users can only lower priority.")
        p("-n N", "设置 nice 值", "Set the niceness to N")
        p("--adjustment=N", "同 -n", "Same as -n")
        e("nice -n 10 ./build.sh", "以较低优先级运行编译任务", "Run a build with lower priority")
        e("sudo nice -n -5 ./realtime", "以更高优先级运行（需要 root）", "Run with higher priority (needs root)")
        rel("renice", "top", "ps")
    }

    c("renice", "调整已运行进程的优先级", "Alter the priority of running processes", "renice [选项] 优先级 -p PID", level = Level.ADVANCED) {
        p("-n N", "要设置的 nice 值", "The niceness to apply")
        p("-p PID", "指定进程", "Target a process id")
        p("-u 用户", "调整某用户所有进程", "Target all processes of a user")
        e("renice -n 15 -p 1234", "把 1234 号进程的优先级调低", "Lower the priority of PID 1234")
        e("sudo renice -n -10 -p 1234", "提高优先级（需要 root）", "Raise the priority (needs root)")
        rel("nice", "top", "ps")
    }

    c("time", "测量命令运行耗时", "Measure how long a command takes", "time [选项] 命令") {
        detail("输出 real（总耗时）、user（用户态 CPU 时间）、sys（内核态 CPU 时间）。", "Reports real (wall clock), user (CPU in user mode) and sys (CPU in kernel mode) time.")
        p("-v", "显示更详细的信息（GNU 版）", "Verbose statistics (GNU version)")
        p("-p", "以标准 POSIX 格式输出", "Portable POSIX output format")
        e("time make", "测量编译耗时", "Measure how long a build takes")
        e("/usr/bin/time -v ./program", "查看内存峰值等详细统计", "Show peak memory and more")
        tip("user+sys 远小于 real 说明程序大部分时间在等待 IO。")
        rel("timeout", "watch", "date")
    }

    c("timeout", "限制命令最长运行时间", "Run a command with a time limit", "timeout [选项] 时长 命令") {
        p("-s 信号", "超时后发送的信号（默认 TERM）", "Signal to send on timeout (default TERM)")
        p("-k N", "TERM 无效后 N 秒再发 KILL", "Also send KILL after N more seconds")
        p("--preserve-status", "保留被终止命令的退出码", "Exit with the command's own status")
        e("timeout 10s curl https://example.com", "最多请求 10 秒", "Give curl at most 10 seconds")
        e("timeout -k 5 30s ./backup.sh", "30 秒后终止，5 秒后强杀", "Kill after 30s, force after 5 more")
        rel("time", "watch", "kill")
    }

    c("ulimit", "查看和限制资源使用", "Get and set user resource limits", "ulimit [选项] [值]", level = Level.ADVANCED) {
        detail("shell 内建命令，控制进程可打开的文件数、可占用的内存与 CPU 时间等。", "A shell builtin that caps open files, memory, CPU time and more for the shell and its children.")
        p("-a", "显示全部限制", "Show all current limits")
        p("-n", "最大打开文件描述符数（服务调优常用）", "Max number of open file descriptors")
        p("-u", "最大进程数", "Max number of user processes")
        p("-f", "最大可创建文件大小", "Max size of files written")
        e("ulimit -a", "查看当前所有限制", "Show every current limit")
        e("ulimit -n 65535", "提高可打开文件数（需要 /etc/security/limits.conf 授权）", "Raise the open-file limit")
        tip("线上服务报 Too many open files 时，除了 ulimit 还要检查 systemd 的 LimitNOFILE。")
        rel("nice", "systemctl", "free")
    }
}
