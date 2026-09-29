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

/** 权限与用户 —— Linux 安全模型的核心 */
val PermUserGroup = group("perm", "权限与用户", "Permissions & Users", "lock") {

    c("chmod", "修改文件权限", "Change file permission bits", "chmod [选项] 权限 文件") {
        detail(
            "权限分三组：属主(u)、属组(g)、其他人(o)，每组有读(r=4)、写(w=2)、执行(x=1)。数字法把三者相加，如 755 表示 rwxr-xr-x。",
            "Permissions apply to the user (u), group (g) and others (o), each with read (4), write (2) and execute (1). 755 means rwxr-xr-x."
        )
        p("755", "属主可读写执行，其他人可读和执行（常用目录）", "rwxr-xr-x — typical for directories")
        p("644", "属主可读写，其他人只读（常用文件）", "rw-r--r-- — typical for files")
        p("600", "仅属主可读写（私钥等敏感文件）", "rw------- — for private keys")
        p("u+x", "给属主增加执行权限", "Add execute for the owner")
        p("-R", "递归修改目录内所有文件", "Apply recursively")
        p("+x", "给所有角色增加执行权限", "Add execute for everyone")
        e("chmod +x deploy.sh", "让脚本可以执行", "Make a script executable")
        e("chmod 600 ~/.ssh/id_rsa", "收紧私钥权限（SSH 要求）", "Tighten permissions on an SSH key")
        e("chmod -R 755 /var/www/html", "递归设置网站目录权限", "Set web directory permissions recursively")
        e("chmod u+w file.txt", "只给属主加写权限", "Give the owner write access")
        tip("目录必须同时有 r 和 x 权限才能进入并列出内容：只给 r 会「看得见进不去」。")
        rel("chown", "umask", "sudo")
    }

    c("chown", "修改文件属主 / 属组", "Change file owner and group", "chown [选项] [属主][:属组] 文件") {
        detail("改文件或目录归谁所有；只有 root 或文件属主（且属于目标组）能执行。", "Changes who owns a file; only root (or the owner, for groups) can do it.")
        p("-R", "递归修改目录内所有内容", "Operate recursively")
        p("-v", "显示修改详情", "Show what changed")
        p("--reference=", "参照另一个文件的属主设置", "Use another file as the reference")
        e("sudo chown alex report.txt", "把文件所有者改为 alex", "Give report.txt to alex")
        e("sudo chown -R www-data:www-data /var/www", "把网站目录交给 www-data 用户和组", "Give the web root to www-data")
        e("sudo chown alex: file.txt", "只改属主，组保持为 alex 的登录组", "Change the owner only")
        rel("chmod", "chgrp", "groups")
    }

    c("chgrp", "修改文件所属组", "Change the group ownership", "chgrp [选项] 组名 文件") {
        p("-R", "递归修改", "Operate recursively")
        e("sudo chgrp developers app.conf", "把配置文件交给 developers 组", "Assign a file to the developers group")
        e("sudo chgrp -R www-data /var/www", "递归修改目录的属组", "Recursively change the group of a directory")
        rel("chown", "chmod", "groupadd")
    }

    c("umask", "设置新建文件的默认权限", "Set the default permission mask", "umask [选项] [掩码]") {
        detail(
            "umask 决定新建文件的初始权限：文件最大 666，目录最大 777，再减去 umask 值。常见 022 会得到文件 644、目录 755。",
            "umask defines the initial mode: files start at 666 and directories at 777, minus the mask. With 022 you get 644 files and 755 directories."
        )
        p("-S", "以符号形式显示（如 u=rwx,g=rx,o=rx）", "Show the mask symbolically")
        p("022", "默认：属主可写，其他人只读", "Default: owner writes, others read")
        p("077", "严格：只有属主能访问", "Strict: owner only")
        e("umask", "查看当前掩码", "Show the current mask")
        e("umask 077", "让之后新建的文件只对属主开放", "Make new files private to you")
        tip("把 umask 077 写进 ~/.bashrc 可以让新建文件默认私有。")
        rel("chmod", "export")
    }

    c("sudo", "以 root 身份执行一条命令", "Execute a command as another user", "sudo [选项] 命令") {
        detail("临时提权，需要输入当前用户自己的密码；比直接 su 到 root 更安全，且所有操作都会记日志。", "Runs one command as root using *your* password; safer than su and every call is logged.")
        p("-i", "切换到 root 的登录 shell", "Start a root login shell")
        p("-u 用户", "以指定用户身份执行", "Run as a given user")
        p("-l", "列出当前用户可执行的 sudo 命令", "List your sudo privileges")
        p("-k", "立即清除缓存的密码", "Invalidate the cached credentials")
        p("!!", "用 sudo 重跑上一条命令", "Re-run the previous command with sudo")
        e("sudo apt update", "以管理员权限更新软件源", "Update package lists as root")
        e("sudo -u postgres psql", "以 postgres 用户执行 psql", "Run psql as the postgres user")
        e("sudo !!", "「忘了加 sudo」时的补救命令", "Retry the last command with sudo")
        tip("忘记加 sudo 时不要重新敲一遍，直接 sudo !! 即可。")
        rel("su", "visudo", "chmod")
    }

    c("su", "切换用户身份", "Switch to another user account", "su [选项] [用户名]") {
        p("-", "模拟完整登录（加载目标用户的环境变量）", "Start a login shell with the target environment")
        p("-c", "以目标用户执行一条命令后返回", "Run one command and return")
        e("su -", "切换到 root 并加载 root 环境", "Become root with a full environment")
        e("su - alex -c 'whoami'", "以 alex 身份执行一条命令", "Run a command as alex")
        tip("日常推荐 sudo，只在需要完整 root 环境时才用 su -。")
        rel("sudo", "whoami", "passwd")
    }

    c("useradd", "新建系统用户", "Create a new user account", "useradd [选项] 用户名") {
        detail("创建用户；Debian/Ubuntu 上更友好的是交互式命令 adduser。", "Creates a user; on Debian/Ubuntu the friendlier interactive alternative is adduser.")
        p("-m", "同时创建家目录", "Create the home directory")
        p("-s", "指定登录 shell，如 /bin/bash", "Set the login shell")
        p("-G", "加入附加组，逗号分隔", "Add supplementary groups")
        p("-u", "指定 UID", "Set the numeric user id")
        p("-r", "创建系统用户（无家目录，用于服务）", "Create a system account")
        e("sudo useradd -m -s /bin/bash alex", "创建带家目录的用户 alex", "Create alex with a home directory and bash")
        e("sudo useradd -r -s /usr/sbin/nologin nginx", "创建服务用的系统账号", "Create a system account for a service")
        rel("usermod", "passwd", "adduser", "groupadd")
    }

    c("usermod", "修改已有用户", "Modify an existing user account", "usermod [选项] 用户名") {
        p("-aG", "追加到附加组（务必加 -a，否则会覆盖原有组）", "Append to supplementary groups (always with -a)")
        p("-s", "修改登录 shell", "Change the login shell")
        p("-L / -U", "锁定 / 解锁账号", "Lock / unlock the account")
        p("-d", "修改家目录", "Change the home directory")
        e("sudo usermod -aG docker alex", "把 alex 加入 docker 组", "Add alex to the docker group")
        e("sudo usermod -s /bin/zsh alex", "把 alex 的 shell 换成 zsh", "Change alex's shell to zsh")
        e("sudo usermod -L olduser", "锁定不再使用的账号", "Lock an unused account")
        tip("-aG 少了 -a 会把用户从其他附加组中移除，这是常见的运维事故。")
        rel("useradd", "groupadd", "id", "groups")
    }

    c("userdel", "删除用户", "Delete a user account", "userdel [选项] 用户名") {
        p("-r", "同时删除家目录和邮件池", "Remove the home directory and mail spool")
        e("sudo userdel -r alex", "删除用户及其家目录", "Delete the user and their home")
        rel("useradd", "groupdel", "rm")
    }

    c("passwd", "修改用户密码", "Change a user's password", "passwd [选项] [用户名]") {
        p("-l / -u", "锁定 / 解锁密码", "Lock / unlock the password")
        p("-e", "强制下次登录时改密码", "Force a password change on next login")
        p("-S", "查看密码状态", "Report password status")
        e("passwd", "修改自己的密码", "Change your own password")
        e("sudo passwd alex", "管理员为 alex 重置密码", "Reset alex's password as root")
        e("sudo passwd -e alex", "要求 alex 下次登录必须改密码", "Force alex to change it at next login")
        rel("useradd", "chage", "su")
    }

    c("groupadd", "新建用户组", "Create a new group", "groupadd [选项] 组名") {
        p("-g", "指定 GID", "Set the numeric group id")
        p("-r", "创建系统组", "Create a system group")
        e("sudo groupadd developers", "创建 developers 组", "Create the developers group")
        rel("usermod", "groupdel", "chgrp")
    }

    c("groupdel", "删除用户组", "Delete a group", "groupdel 组名") {
        e("sudo groupdel developers", "删除 developers 组", "Delete the developers group")
        tip("如果某用户把它当作主组，需要先改掉该用户的主组才能删除。")
        rel("groupadd", "usermod")
    }

    c("id", "查看用户和组的 ID", "Print user and group identities", "id [选项] [用户名]") {
        p("-u", "只显示 UID", "Print only the user id")
        p("-g", "只显示主组 GID", "Print only the effective group id")
        p("-G", "显示所有附加组 GID", "Print all group ids")
        p("-n", "配合上面参数显示名称而非数字", "Show names instead of numbers")
        e("id", "查看自己的 UID、GID 和所属组", "Show your identity and groups")
        e("id alex", "查看 alex 的身份信息", "Show alex's identity")
        e("id -u", "脚本里判断是否 root 常用（值为 0）", "Print your UID (0 means root)")
        rel("whoami", "groups", "usermod")
    }

    c("whoami", "显示当前用户名", "Print the current user name", "whoami") {
        e("whoami", "输出当前登录的用户", "Print the effective user name")
        e("[ \"\$(id -u)\" -eq 0 ] && echo root", "脚本中判断是否为 root", "Test for root inside a script")
        rel("id", "who", "su")
    }

    c("who", "查看当前登录的用户", "Show who is logged on", "who [选项]") {
        p("-a", "显示更完整的信息", "Show all information")
        p("-b", "显示系统上次启动时间", "Show the last system boot time")
        e("who", "列出已登录的用户和终端", "List logged-in users and terminals")
        e("who -b", "查看机器上次开机时间", "Show when the machine last booted")
        rel("w", "last", "uptime")
    }

    c("w", "查看登录用户在做什么", "Show who is logged on and what they are doing", "w [选项] [用户]") {
        detail("比 who 多出系统负载和每个用户正在执行的命令。", "Like who, plus load averages and what each user is running.")
        e("w", "显示在线用户、负载和当前命令", "Show users, load averages and current commands")
        e("w alex", "只看 alex 的会话", "Show only alex's sessions")
        rel("who", "uptime", "ps")
    }

    c("last", "查看登录历史", "Show a listing of last logged-in users", "last [选项]") {
        p("-n N", "只显示最近 N 条", "Show only the last N entries")
        e("last -n 10", "查看最近 10 次登录记录", "Show the 10 most recent logins")
        e("last reboot", "查看系统重启历史", "Show reboot history")
        rel("who", "lastlog", "journalctl")
    }

    c("visudo", "安全地编辑 sudo 配置", "Safely edit the sudoers file", "visudo [选项]") {
        detail("带语法检查地编辑 /etc/sudoers，写错了也不会把自己锁在系统外面。", "Edits /etc/sudoers with syntax checking, so a typo cannot lock you out.")
        p("-c", "只检查配置文件语法", "Check the syntax only")
        p("-f", "编辑指定的 sudoers 文件", "Edit a specific sudoers file")
        e("sudo visudo", "编辑 sudo 主配置", "Edit the main sudoers file")
        e("echo 'alex ALL=(ALL) NOPASSWD:ALL' | sudo tee /etc/sudoers.d/alex", "给 alex 免密 sudo（谨慎使用）", "Grant alex passwordless sudo (use with care)")
        tip("自定义规则请放在 /etc/sudoers.d/ 下的独立文件里，不要直接改主配置。")
        rel("sudo", "tee")
    }

    c("setfacl", "设置更细粒度的 ACL 权限", "Set file access control lists", "setfacl [选项] 规则 文件", level = Level.ADVANCED) {
        detail("传统权限只能给一个属主和一个属组，ACL 可以单独授权给多个用户或组。", "Classic permissions cover one owner and one group; ACLs can grant access to several users or groups.")
        p("-m", "修改或新增一条规则", "Modify or add an entry")
        p("-x", "删除一条规则", "Remove an entry")
        p("-b", "删除所有扩展 ACL", "Remove all extended entries")
        p("-R", "递归设置", "Apply recursively")
        e("setfacl -m u:alex:rw shared.txt", "单独给 alex 读写权限", "Give alex read-write access")
        e("setfacl -m g:devs:rwx /srv/project", "给 devs 组访问项目目录", "Give the devs group access to a project")
        e("getfacl shared.txt", "查看 ACL 规则", "Show the ACL entries")
        tip("分区需要以 acl 选项挂载才支持；ext4/xfs 现代发行版一般默认开启。")
        rel("getfacl", "chmod", "chown")
    }

    c("getfacl", "查看文件的 ACL 权限", "Show file access control lists", "getfacl [选项] 文件", level = Level.ADVANCED) {
        p("-R", "递归查看目录", "List ACLs recursively")
        p("-p", "不去掉路径前的 /", "Do not strip leading slashes")
        e("getfacl /srv/project", "查看目录的 ACL 授权情况", "Show ACL entries of a directory")
        rel("setfacl", "ls")
    }

    c("chage", "管理账号有效期", "Change user password expiry information", "chage [选项] 用户名", level = Level.ADVANCED) {
        p("-l", "查看账号有效期信息", "Show account ageing information")
        p("-M N", "密码 N 天后过期", "Set the maximum password age")
        p("-E", "设置账号过期日期", "Set the account expiry date")
        e("sudo chage -l alex", "查看 alex 的密码策略", "Show alex's password policy")
        e("sudo chage -M 90 alex", "要求 90 天改一次密码", "Force a password change every 90 days")
        rel("passwd", "usermod")
    }
}
