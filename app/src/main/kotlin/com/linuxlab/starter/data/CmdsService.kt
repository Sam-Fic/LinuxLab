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

/** 服务与系统管理（systemd） */
val ServiceGroup = group("service", "服务与系统管理", "Services & systemd", "misc") {

    c("systemctl", "管理系统服务（核心）", "Control the systemd system and service manager", "systemctl [子命令] [服务名]") {
        detail(
            "现代 Linux 的服务管家：启动、停止、开机自启、查看状态都用它。服务名通常省略 .service 后缀。",
            "The modern service manager: start, stop, enable and inspect services. The .service suffix is usually optional."
        )
        p("start", "启动服务", "Start a service")
        p("stop", "停止服务", "Stop a service")
        p("restart", "重启服务", "Restart a service")
        p("reload", "重载配置（不中断连接）", "Reload the config without dropping connections")
        p("enable / disable", "设置 / 取消开机自启", "Enable / disable start at boot")
        p("status", "查看服务状态和最近日志", "Show status and recent logs")
        p("is-active", "只判断服务是否在运行", "Check whether a service is active")
        p("--failed", "列出启动失败的服务", "List failed units")
        p("daemon-reload", "修改服务文件后重新加载配置", "Reload unit files after editing them")
        e("sudo systemctl status nginx", "查看 nginx 运行状态", "Check the status of nginx")
        e("sudo systemctl enable --now nginx", "设置开机自启并立即启动", "Enable at boot and start now")
        e("sudo systemctl restart sshd", "重启 SSH 服务", "Restart the SSH service")
        e("systemctl --failed", "查看启动失败的服务", "Show failed units")
        e("sudo systemctl daemon-reload", "修改 .service 文件后必须执行", "Run after editing a unit file")
        tip("enable 只负责「开机自启」，start 才负责「现在启动」，两者常一起用：enable --now。")
        rel("journalctl", "service", "ps", "shutdown")
    }

    c("journalctl", "查看 systemd 日志", "Query the systemd journal", "journalctl [选项]", level = Level.ADVANCED) {
        detail("集中管理系统日志，取代了大部分 /var/log 下的文本日志。", "Central logging for systemd, replacing many plain-text logs under /var/log.")
        p("-u 服务", "只看某个服务的日志", "Show logs for one unit")
        p("-f", "实时跟踪最新日志", "Follow new entries")
        p("-n N", "只显示最后 N 条", "Show only the last N entries")
        p("--since / --until", "按时间过滤，如 '1 hour ago'", "Filter by time range")
        p("-p 级别", "按级别过滤：err / warning / info", "Filter by priority")
        p("-k", "只看内核日志（等同 dmesg）", "Show kernel messages only")
        p("--disk-usage", "查看日志占用空间", "Show journal disk usage")
        e("sudo journalctl -u nginx -f", "实时查看 nginx 日志", "Follow nginx logs live")
        e("journalctl -u sshd --since '1 hour ago'", "查看一小时内 SSH 登录日志", "Show SSH logs from the last hour")
        e("journalctl -p err -b", "查看本次开机后的错误日志", "Show errors since the current boot")
        e("sudo journalctl --vacuum-size=500M", "把日志清理到 500MB 以内", "Shrink the journal to 500 MB")
        tip("服务起不来先看 journalctl -u 服务名 -n 50，错误信息基本都在里面。")
        rel("systemctl", "dmesg", "tail")
    }

    c("service", "兼容旧脚本的服务管理", "Run a System V init script", "service 服务名 动作", level = Level.ADVANCED) {
        detail("老的 SysVinit 命令，在 systemd 系统上会被转发给 systemctl，仍可正常使用。", "The legacy SysVinit command; on systemd systems it is simply forwarded to systemctl.")
        e("sudo service nginx restart", "重启 nginx", "Restart nginx")
        e("sudo service --status-all", "列出所有服务状态", "List all services and their state")
        rel("systemctl", "init", "ps")
    }

    c("crontab", "设置定时任务", "Schedule periodic background jobs", "crontab [选项] [文件]") {
        detail(
            "格式：分 时 日 月 周 命令。五个时间字段用空格分隔，* 表示任意值，/N 表示每隔 N。",
            "Format: minute hour day-of-month month day-of-week command. '*' means any value and '/N' means every N."
        )
        p("-l", "列出当前用户的定时任务", "List the current crontab")
        p("-e", "编辑定时任务", "Edit the crontab")
        p("-r", "删除所有定时任务", "Remove the crontab")
        p("-u 用户", "操作指定用户的任务（需 root）", "Operate on another user's crontab")
        e("crontab -l", "查看自己的定时任务", "Show your scheduled jobs")
        e("crontab -e", "编辑定时任务", "Edit your scheduled jobs")
        e("0 3 * * * /opt/scripts/backup.sh >> /var/log/backup.log 2>&1", "每天凌晨 3 点执行备份", "Run a backup every day at 3 a.m.")
        e("*/5 * * * * curl -fsS https://example.com/health || echo down", "每 5 分钟检查一次服务健康", "Health check every five minutes")
        e("@reboot /home/alex/start.sh", "开机后执行一次脚本", "Run a script once at boot")
        tip("cron 里环境变量很少，脚本中请用绝对路径，并在末尾加 >> 日志文件 2>&1 便于排查。")
        rel("at", "systemd-timer", "date", "systemctl")
    }

    c("at", "安排一次性定时任务", "Schedule a command to run once", "at 时间", level = Level.ADVANCED) {
        p("atq", "查看待执行的任务", "List pending jobs")
        p("atrm N", "删除第 N 号任务", "Delete job N")
        p("-f 脚本", "从文件读取要执行的命令", "Read the job from a file")
        e("echo 'shutdown -h now' | at 23:00", "晚上 11 点关机", "Shut down at 23:00")
        e("at now + 10 minutes", "10 分钟后执行（随后输入命令，Ctrl+D 结束）", "Schedule ten minutes from now")
        e("atq", "查看已排队的任务", "List scheduled jobs")
        rel("crontab", "sleep", "date")
    }

    c("timedatectl", "查看与设置时间和时区", "Control the system clock and time zone", "timedatectl [选项]") {
        p("status", "显示当前时间与时区状态", "Show the current time settings")
        p("set-timezone", "设置时区，如 Asia/Shanghai", "Set the time zone")
        p("set-ntp true", "开启网络时间同步", "Enable NTP synchronisation")
        p("list-timezones", "列出所有可用时区", "List available time zones")
        e("timedatectl", "查看时间、时区与 NTP 状态", "Show time, zone and NTP status")
        e("sudo timedatectl set-timezone Asia/Shanghai", "设置时区为上海", "Set the time zone to Asia/Shanghai")
        e("sudo timedatectl set-ntp true", "开启自动对时", "Turn on automatic time sync")
        tip("服务器时间不准会让日志和证书校验出问题，部署后第一件事就是确认时区与 NTP。")
        rel("date", "cal", "hostnamectl")
    }

    c("hostnamectl", "查看与设置主机信息", "Control the system hostname", "hostnamectl [选项]") {
        p("status", "显示主机名、系统版本与内核", "Show host name, OS and kernel")
        p("set-hostname", "设置主机名", "Set the host name")
        p("--pretty", "设置易读的主机名（可含空格）", "Set a pretty host name")
        e("hostnamectl", "查看主机与系统信息", "Show host and OS information")
        e("sudo hostnamectl set-hostname web-01", "把主机名改为 web-01", "Rename the host to web-01")
        rel("hostname", "uname", "timedatectl")
    }

    c("shutdown", "关机或重启", "Power off or reboot the machine", "shutdown [选项] 时间 [消息]") {
        p("-h now", "立即关机", "Halt immediately")
        p("-r now", "立即重启", "Reboot immediately")
        p("-c", "取消已安排的关机", "Cancel a pending shutdown")
        p("+N", "N 分钟后执行", "Schedule N minutes from now")
        e("sudo shutdown -h now", "立即关机", "Power off now")
        e("sudo shutdown -r +10 '系统升级，请保存工作'", "10 分钟后重启并广播通知", "Reboot in 10 minutes with a message")
        e("sudo shutdown -c", "取消刚才安排的关机", "Cancel the pending shutdown")
        tip("远程维护时先安排延时重启（如 +5），万一配置写错还来得及 shutdown -c 取消。")
        rel("reboot", "poweroff", "init", "systemctl")
    }

    c("reboot", "重启系统", "Reboot the machine", "reboot [选项]") {
        p("-f", "强制重启（不调用 shutdown）", "Force reboot without contacting the manager")
        e("sudo reboot", "立即重启", "Reboot now")
        e("sudo reboot -f", "强制重启（系统卡住时）", "Force an immediate reboot")
        rel("shutdown", "poweroff", "init")
    }

    c("init", "切换系统运行级别", "Change the SysV runlevel", "init [0-6]", level = Level.ADVANCED) {
        detail("传统运行级别：0 关机、1 单用户、3 多用户命令行、5 图形界面、6 重启。", "Legacy runlevels: 0 halt, 1 single user, 3 multi-user text, 5 graphical, 6 reboot.")
        e("sudo init 0", "关机", "Halt the system")
        e("sudo init 6", "重启", "Reboot the system")
        e("sudo init 3", "切换到纯命令行模式", "Switch to text mode")
        tip("systemd 系统上推荐用 systemctl isolate multi-user.target 代替 init 3。")
        rel("runlevel", "shutdown", "systemctl")
    }

    c("runlevel", "查看当前运行级别", "Print the previous and current runlevel", "runlevel", level = Level.ADVANCED) {
        e("runlevel", "输出上一个和当前运行级别", "Show the previous and current runlevel")
        e("systemctl get-default", "查看 systemd 默认目标", "Show the default systemd target")
        rel("init", "systemctl", "shutdown")
    }

    c("loginctl", "管理登录会话", "Control the systemd login manager", "loginctl [子命令]", level = Level.ADVANCED) {
        p("list-sessions", "列出当前所有会话", "List current sessions")
        p("list-users", "列出已登录用户", "List logged-in users")
        p("terminate-session ID", "结束指定会话", "Kill a session")
        e("loginctl list-sessions", "查看当前登录会话", "Show active sessions")
        e("loginctl", "查看所有会话与用户信息", "Show sessions, seats and users")
        rel("who", "w", "systemctl")
    }

    c("systemd-analyze", "分析开机耗时", "Analyse system boot-up performance", "systemd-analyze [子命令]", level = Level.ADVANCED) {
        p("time", "显示内核和用户空间各花多久", "Show how long the boot took")
        p("blame", "按耗时列出所有服务", "List units ordered by start-up time")
        p("critical-chain", "显示启动关键路径", "Show the critical chain of units")
        e("systemd-analyze", "查看总开机耗时", "Show total boot time")
        e("systemd-analyze blame | head", "找出拖慢开机的服务", "Find the slowest units at boot")
        rel("systemctl", "journalctl", "uptime")
    }

    c("cron 时间写法", "记住五个时间字段", "Master the five time fields", "* * * * * 命令", level = Level.ADVANCED) {
        detail(
            "从左到右依次是：分钟(0-59)、小时(0-23)、日(1-31)、月(1-12)、星期(0-7，0 和 7 都是周日)。",
            "Fields left to right: minute (0-59), hour (0-23), day of month (1-31), month (1-12), day of week (0-7, both 0 and 7 mean Sunday)."
        )
        e("*/10 * * * * cmd", "每 10 分钟执行一次", "Every ten minutes")
        e("30 2 * * 1 cmd", "每周一凌晨 2:30 执行", "Every Monday at 2:30")
        e("0 0 1 * * cmd", "每月 1 号零点执行", "At midnight on the first of each month")
        e("@daily cmd", "每天执行一次（简写）", "Once a day (shorthand)")
        tip("在线校验 cron 表达式可以用 crontab.guru 这类网站。")
        rel("crontab", "at", "date")
    }

    c("sudo 免密与权限细化", "安全地分配管理权限", "Grant limited administrative rights", "sudo visudo / etc/sudoers.d", level = Level.ADVANCED) {
        detail("不要把完整 root 权限随便给人：可以把 sudo 限制到具体命令。", "Instead of full root, grant sudo rights for specific commands only.")
        e("echo 'alex ALL=(ALL) NOPASSWD:/usr/bin/systemctl restart nginx' | sudo tee /etc/sudoers.d/alex", "只允许 alex 免密重启 nginx", "Allow alex to restart nginx without a password")
        e("sudo chmod 440 /etc/sudoers.d/alex", "收紧 sudoers 文件权限", "Tighten permissions on the sudoers file")
        e("sudo -l -U alex", "检查 alex 实际拥有的 sudo 权限", "Check which commands alex may run")
        tip("sudoers 文件权限必须是 440，否则 sudo 会拒绝加载。")
        rel("sudo", "visudo", "chmod", "tee")
    }
}
