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

import com.linuxlab.starter.model.FhsKind
import com.linuxlab.starter.model.FhsNode

/**
 * Linux 文件系统层次结构标准（FHS）的目录树。
 *
 * 说明按 FHS 3.0 与主流发行版（Debian / Ubuntu / RHEL / Arch）的实际情况写，
 * 并标注了那些「现在其实已经软链接到 /usr」的历史目录，避免初学者被老教程绕晕。
 */
val FhsTree: List<FhsNode> = listOf(

    FhsNode(
        path = "/",
        kind = FhsKind.SYSTEM,
        name = "/",
        zh = "一切的开始：根目录，所有目录都是它的子目录",
        detail = "Linux 没有盘符（没有 C 盘 D 盘）。所有硬盘、U 盘、网络存储都要「挂载」到这棵唯一目录树上的某个空目录里，之后才能访问。根目录本身尽量只放一级目录，不放普通文件。",
        files = listOf(
            "/bin → /usr/bin —— 基本命令（为兼容保留的软链接）",
            "/sbin → /usr/sbin —— 系统管理命令",
            "/lib → /usr/lib —— 基本共享库"
        ),
        commands = listOf("ls -l /", "df -h", "lsblk"),
        tips = listOf(
            "只有 root 能往 / 下直接写文件，普通用户写东西请去自己的家目录",
            "「挂载」= 把一块存储接到目录树的某个位置：mount /dev/sdb1 /mnt"
        )
    ),

    FhsNode(
        path = "/bin",
        kind = FhsKind.SYSTEM,
        name = "bin",
        zh = "所有用户都能用的基本命令（ls、cp、bash…）",
        detail = "历史上这里放的是「单用户模式也要能用的命令」。现代发行版（Debian / Ubuntu / RHEL / Arch 的新版本）已把它软链接到 /usr/bin，两者内容完全一样。",
        files = listOf(
            "ls / cp / mv / rm —— 文件操作",
            "bash / sh —— Shell 解释器",
            "cat / echo —— 最基本的文本命令"
        ),
        commands = listOf("ls -l /bin | head", "readlink -f /bin"),
        tips = listOf("/bin 与 /usr/bin 现在是同一个目录，改哪个都一样")
    ),

    FhsNode(
        path = "/sbin",
        kind = FhsKind.SYSTEM,
        name = "sbin",
        zh = "系统管理命令，主要给 root 用（fsck、ip、mount）",
        detail = "s = system。这里的命令大多需要 root 权限，普通用户直接运行通常只会得到「Permission denied」。同样，新发行版里它已经软链接到 /usr/sbin。",
        files = listOf(
            "ip / ifconfig —— 网络配置",
            "mount / umount —— 挂载与卸载",
            "fsck —— 磁盘检查修复",
            "reboot / shutdown —— 关机重启"
        ),
        commands = listOf("sudo ip a", "ls /sbin | head"),
        tips = listOf("命令找不到时先试 sudo：很多管理命令只装在 sbin 里")
    ),

    FhsNode(
        path = "/usr",
        kind = FhsKind.SYSTEM,
        name = "usr",
        zh = "只读的「第二层目录」：绝大多数程序都装在这里",
        detail = "usr 不是 user（用户），而是 Unix System Resources。它自己是只读的、可在多台机器间共享的一整层：装上系统后几乎不变，只有装 / 卸软件时才会动。",
        files = listOf(),
        commands = listOf("ls /usr", "du -sh /usr 2>/dev/null"),
        tips = listOf(
            "/usr 下是「系统装的软件」；自己编译安装的请放 /usr/local，别混在一起",
            "重装系统时保留 /home 和 /usr/local 就能保住大部分个人数据"
        ),
        children = listOf(
            FhsNode(
                path = "/usr/bin",
                kind = FhsKind.SYSTEM,
                name = "bin",
                zh = "绝大多数用户命令都在这里",
                detail = "你每天敲的 git、python3、curl、vim 基本都躺在 /usr/bin。Shell 就是靠 PATH 环境变量在这里逐个目录找命令的。",
                files = listOf("python3 / git / curl / vim —— 用户级程序"),
                commands = listOf("which python3", "echo \$PATH")
            ),
            FhsNode(
                path = "/usr/sbin",
                kind = FhsKind.SYSTEM,
                name = "sbin",
                zh = "非关键的系统管理命令（nginx、sshd、useradd）",
                detail = "开机早期不必须用得到的管理程序放这里：服务程序、用户管理、守护进程。",
                files = listOf("sshd / nginx / useradd —— 服务与管理程序"),
                commands = listOf("ls /usr/sbin | head")
            ),
            FhsNode(
                path = "/usr/lib",
                kind = FhsKind.SYSTEM,
                name = "lib",
                zh = "程序运行时要用的共享库（.so 文件）",
                detail = "相当于 Windows 的 DLL。报「error while loading shared libraries: libxxx.so.0」就是这里缺文件。",
                files = listOf("libc.so.6 —— C 库，几乎所有程序都依赖它"),
                commands = listOf("ldd /bin/ls", "ldconfig -p | head")
            ),
            FhsNode(
                path = "/usr/local",
                kind = FhsKind.SYSTEM,
                name = "local",
                zh = "管理员自己编译安装的软件，包管理器不管这里",
                detail = "make install 的默认目标目录。里面的软件不会被 apt / dnf 覆盖，也不会被它们升级——所以要自己记得更新。",
                files = listOf(
                    "/usr/local/bin —— 自己装的程序的命令",
                    "/usr/local/etc —— 自己装的程序的配置"
                ),
                commands = listOf("ls /usr/local/bin", "./configure --prefix=/usr/local && make && sudo make install")
            ),
            FhsNode(
                path = "/usr/share",
                kind = FhsKind.SYSTEM,
                name = "share",
                zh = "与 CPU 架构无关的只读数据：文档、man 手册、时区、字体",
                detail = "同一份数据 x86 和 ARM 机器都能用，所以单独放这里，方便多机共享。",
                files = listOf(
                    "/usr/share/man —— man 手册页",
                    "/usr/share/zoneinfo —— 时区数据",
                    "/usr/share/doc —— 软件包文档"
                ),
                commands = listOf("man ls", "ls /usr/share/zoneinfo/Asia")
            ),
            FhsNode(
                path = "/usr/src",
                kind = FhsKind.SYSTEM,
                name = "src",
                zh = "源码：内核头文件、自己编译的模块",
                detail = "编译驱动或内核模块时需要 /usr/src/linux-headers-$(uname -r)。",
                files = listOf("/usr/src/linux-headers-* —— 内核头文件"),
                commands = listOf("uname -r", "ls /usr/src")
            )
        )
    ),

    FhsNode(
        path = "/etc",
        kind = FhsKind.CONFIG,
        name = "etc",
        zh = "系统和程序的配置文件，几乎全是纯文本",
        detail = "改 Linux 的绝大多数行为都不需要改注册表、也不需要重装——改 /etc 下对应的文本文件，然后重启服务即可。改之前先备份一份是好习惯。",
        files = listOf(
            "/etc/passwd —— 用户账号信息（不含密码）",
            "/etc/shadow —— 用户密码散列，只有 root 能读",
            "/etc/group —— 用户组信息",
            "/etc/fstab —— 开机自动挂载表",
            "/etc/hosts —— 本地域名解析（改 hosts 就是改这里）",
            "/etc/resolv.conf —— DNS 服务器地址",
            "/etc/ssh/sshd_config —— SSH 服务端配置",
            "/etc/apt/sources.list —— Debian / Ubuntu 软件源",
            "/etc/os-release —— 发行版名字和版本号",
            "/etc/crontab —— 系统级定时任务"
        ),
        commands = listOf("cat /etc/os-release", "ls /etc | head -30", "sudo cp sshd_config sshd_config.bak"),
        tips = listOf(
            "改配置前先备份：sudo cp 文件名 文件名.bak，改崩了还能救回来",
            "不知道配置改没生效：systemctl restart 服务名"
        )
    ),

    FhsNode(
        path = "/var",
        kind = FhsKind.DATA,
        name = "var",
        zh = "经常变化的数据：日志、缓存、队列、数据库文件",
        detail = "variable = 会变的。与只读的 /usr 相对：这里的东西天天在长。磁盘被占满，十有八九是 /var 撑爆的（尤其是 /var/log）。",
        files = listOf(),
        commands = listOf("sudo du -sh /var/* | sort -h", "df -h /var"),
        tips = listOf(
            "磁盘满时先查它：sudo du -sh /var/* | sort -h | tail",
            "清理日志用 journalctl --vacuum-size=200M，别直接 rm 正在写入的日志文件"
        ),
        children = listOf(
            FhsNode(
                path = "/var/log",
                kind = FhsKind.DATA,
                name = "log",
                zh = "系统和服务的日志，排错第一站",
                detail = "任何「它为什么不工作了」的问题，第一步几乎都是来这里翻日志。Debian 系看 syslog / auth.log，RHEL 系看 messages / secure。",
                files = listOf(
                    "/var/log/syslog 或 messages —— 系统综合日志",
                    "/var/log/auth.log 或 secure —— 登录、sudo、SSH 认证记录",
                    "/var/log/nginx/、/var/log/mysql/ —— 各服务自己的日志目录"
                ),
                commands = listOf("sudo tail -f /var/log/syslog", "journalctl -xe", "sudo tail -20 /var/log/auth.log")
            ),
            FhsNode(
                path = "/var/cache",
                kind = FhsKind.DATA,
                name = "cache",
                zh = "应用缓存：可以删，删了会自动重建",
                detail = "apt 下载的安装包就缓存在 /var/cache/apt/archives，占空间可以直接清。",
                files = listOf("/var/cache/apt/archives —— apt 下载的安装包"),
                commands = listOf("sudo apt clean", "du -sh /var/cache/*")
            ),
            FhsNode(
                path = "/var/lib",
                kind = FhsKind.DATA,
                name = "lib",
                zh = "程序运行中的状态数据：数据库、包管理器状态",
                detail = "这里的数据不能随便删——删了等于程序失忆。MySQL 的数据文件、Docker 的镜像都在这里。",
                files = listOf(
                    "/var/lib/mysql —— MySQL 数据库文件",
                    "/var/lib/docker —— Docker 镜像与容器",
                    "/var/lib/dpkg —— 已安装软件包清单"
                ),
                commands = listOf("du -sh /var/lib/* 2>/dev/null | sort -h")
            ),
            FhsNode(
                path = "/var/spool",
                kind = FhsKind.DATA,
                name = "spool",
                zh = "队列数据：待发的邮件、打印任务、cron",
                detail = "spool 原意是「卷轴/排队」。at、cron、打印服务把待处理的任务放在这里。",
                files = listOf("/var/spool/cron —— 用户的定时任务"),
                commands = listOf("ls /var/spool", "crontab -l")
            ),
            FhsNode(
                path = "/var/tmp",
                kind = FhsKind.DATA,
                name = "tmp",
                zh = "重启后仍然保留的临时文件",
                detail = "和 /tmp 的区别：/var/tmp 里的文件默认保留更久（通常 30 天），/tmp 重启或 10 天不用就清。",
                files = listOf(),
                commands = listOf("ls -ld /var/tmp")
            ),
            FhsNode(
                path = "/var/www",
                kind = FhsKind.DATA,
                name = "www",
                zh = "网页默认根目录（Nginx / Apache 的默认站点目录）",
                detail = "把网页文件放这里，浏览器访问服务器 IP 就能看到。注意权限：Web 服务进程（www-data / nginx）必须能读。",
                files = listOf("/var/www/html/index.html —— 默认首页"),
                commands = listOf("ls -l /var/www/html", "sudo chown -R www-data:www-data /var/www/html")
            )
        )
    ),

    FhsNode(
        path = "/home",
        kind = FhsKind.USER,
        name = "home",
        zh = "普通用户的家目录，一人一个子目录",
        detail = "你自己的文件、下载、桌面、个人配置全在这里。`cd ~` 或 `cd` 直接回到家目录，路径里的 ~ 就代表它。重新安装系统时保留 /home 就能保住个人数据。",
        files = listOf(
            "/home/用户名 —— 每个用户自己的目录",
            "~/.bashrc —— 个人 Shell 配置（每次开终端都会执行）",
            "~/.ssh/ —— SSH 密钥与 known_hosts",
            "~/.config/ —— 各应用的个人配置"
        ),
        commands = listOf("cd ~ && pwd", "ls -la ~", "echo \$HOME"),
        tips = listOf(
            "~ 就是 /home/你的用户名，root 的 ~ 是 /root",
            "以点开头的 .bashrc 是隐藏文件，要用 ls -a 才看得到"
        )
    ),

    FhsNode(
        path = "/root",
        kind = FhsKind.USER,
        name = "root",
        zh = "root（超级管理员）的家目录",
        detail = "注意它不是 /home/root。出于安全考虑，这个目录默认连普通用户都进不去（权限 700）。",
        files = listOf("/root/.bashrc —— root 的 Shell 配置"),
        commands = listOf("sudo ls -la /root", "sudo -i"),
        tips = listOf("日常操作别用 root 登录，用 sudo 临时提权更安全")
    ),

    FhsNode(
        path = "/dev",
        kind = FhsKind.DEVICE,
        name = "dev",
        zh = "设备文件：「Linux 下一切皆文件」的集中体现",
        detail = "硬盘、终端、键盘、随机数发生器……硬件在这里都被抽象成文件，读写文件就等于操作设备。这些文件不占磁盘空间，由内核在开机时生成。",
        files = listOf(
            "/dev/sda、/dev/nvme0n1 —— 硬盘",
            "/dev/sda1 —— 硬盘上的第 1 个分区",
            "/dev/null —— 黑洞：丢进去的数据全消失",
            "/dev/zero —— 无限输出 0",
            "/dev/random、/dev/urandom —— 随机数",
            "/dev/tty、/dev/pts/0 —— 终端与伪终端"
        ),
        commands = listOf("ls -l /dev | head -20", "lsblk", "cat /dev/null"),
        tips = listOf(
            "不想看命令的输出？丢进黑洞：命令 > /dev/null 2>&1",
            "「/dev/sda」的命名会随插拔变化，永久挂载请用 UUID（/etc/fstab 里写的就是 UUID）"
        )
    ),

    FhsNode(
        path = "/proc",
        kind = FhsKind.DEVICE,
        name = "proc",
        zh = "虚拟文件系统：内核和进程的实时状态，不占磁盘",
        detail = "这里的文件都是内核现算出来的，大小显示为 0，内容却是实时的。ps、top、free 这类命令读的就是这里。",
        files = listOf(
            "/proc/cpuinfo —— CPU 信息",
            "/proc/meminfo —— 内存信息（free 读它）",
            "/proc/uptime —— 开机多久了",
            "/proc/loadavg —— 系统平均负载",
            "/proc/[PID]/ —— 某个进程的详细信息"
        ),
        commands = listOf("cat /proc/cpuinfo | head", "cat /proc/meminfo | head -3", "cat /proc/loadavg"),
        tips = listOf("调试程序时 cat /proc/进程号/status 能看到它的内存、线程数等细节")
    ),

    FhsNode(
        path = "/sys",
        kind = FhsKind.DEVICE,
        name = "sys",
        zh = "虚拟文件系统：内核设备与驱动的参数（sysfs）",
        detail = "把内核里的设备树导出成文件。改硬件参数、查网卡速率、控制 LED 都在这里。原则上只读查看，别乱写。",
        files = listOf(
            "/sys/class/net/ —— 网卡信息与速率",
            "/sys/class/backlight/ —— 屏幕亮度",
            "/sys/block/ —— 块设备信息"
        ),
        commands = listOf("ls /sys/class/net", "cat /sys/class/net/eth0/speed 2>/dev/null"),
        tips = listOf("看网卡速率：cat /sys/class/net/网卡名/speed（单位 Mb/s）")
    ),

    FhsNode(
        path = "/tmp",
        kind = FhsKind.DATA,
        name = "tmp",
        zh = "临时文件：谁都能写，重启通常清空",
        detail = "权限是 1777（最后那个 1 是 sticky 位：谁建的谁才能删，防止互相删文件）。别把重要东西放这里。",
        files = listOf("/tmp 下的内容每次重启可能被清空"),
        commands = listOf("ls -ld /tmp", "mktemp"),
        tips = listOf(
            "权限里的 t（drwxrwxrwt）就是 sticky 位：大家都能建文件，但只能删自己的",
            "写脚本要临时文件时用 mktemp，别自己拍脑袋取名字"
        )
    ),

    FhsNode(
        path = "/boot",
        kind = FhsKind.MISC,
        name = "boot",
        zh = "启动相关：内核、initramfs、GRUB 引导程序",
        detail = "开机时 BIOS/UEFI 先读这里。这个目录满了会导致无法升级内核；误删则开不了机。",
        files = listOf(
            "/boot/vmlinuz-* —— Linux 内核本体",
            "/boot/initrd.img-* —— 开机用的临时根文件系统",
            "/boot/grub/ —— GRUB 引导配置"
        ),
        commands = listOf("ls -lh /boot", "uname -r", "df -h /boot"),
        tips = listOf("/boot 只剩几十 MB 时先卸载旧内核：sudo apt autoremove")
    ),

    FhsNode(
        path = "/lib",
        kind = FhsKind.SYSTEM,
        name = "lib",
        zh = "最基本的共享库和内核模块（多已链接到 /usr/lib）",
        detail = "开机早期 /usr 还没挂载时就要用到的库放这里。内核模块在 /lib/modules/$(uname -r)/。",
        files = listOf(
            "/lib/modules/ —— 内核模块（.ko 文件）",
            "/lib/systemd/ —— systemd 单元文件的默认位置"
        ),
        commands = listOf("ls /lib/modules/$(uname -r) | head", "lsmod | head")
    ),

    FhsNode(
        path = "/opt",
        kind = FhsKind.MISC,
        name = "opt",
        zh = "第三方独立软件：一个软件一个目录",
        detail = "optional。商业软件、不按 FHS 打包的程序（如某些 IDE、国产软件）常整包丢在这里，卸载时直接删目录即可。",
        files = listOf("/opt/google/chrome/、/opt/软件名/ —— 一个软件一个目录"),
        commands = listOf("ls /opt")
    ),

    FhsNode(
        path = "/mnt",
        kind = FhsKind.MISC,
        name = "mnt",
        zh = "临时挂载点：管理员手动挂载设备的地方",
        detail = "mount 命令的老习惯位置。临时挂块硬盘、挂个 ISO、挂个 NFS 都用它。",
        files = listOf(),
        commands = listOf("sudo mount /dev/sdb1 /mnt", "sudo umount /mnt")
    ),

    FhsNode(
        path = "/media",
        kind = FhsKind.MISC,
        name = "media",
        zh = "自动挂载的可移动设备：U 盘、光盘、移动硬盘",
        detail = "插上 U 盘后桌面环境会自动在这里建一个子目录并挂载；用完要先「安全弹出」（umount）再拔。",
        files = listOf("/media/用户名/U盘卷标 —— 自动挂载的位置"),
        commands = listOf("ls /media/\$USER", "sudo umount /media/\$USER/*")
    ),

    FhsNode(
        path = "/srv",
        kind = FhsKind.MISC,
        name = "srv",
        zh = "本机对外提供服务的数据（如站点文件、FTP 目录）",
        detail = "service。强调「这台机器是服务器」：对外提供的数据放这里，而不是 /var 或 /home。",
        files = listOf("/srv/www/、/srv/ftp/ —— 对外服务的站点数据"),
        commands = listOf("ls /srv")
    ),

    FhsNode(
        path = "/run",
        kind = FhsKind.MISC,
        name = "run",
        zh = "开机以来的运行时数据：PID 文件、socket、锁",
        detail = "内容存在内存里，每次重启清空。以前这些东西散落在 /var/run，现在统一到 /run（/var/run 是它的软链接）。",
        files = listOf("/run/*.pid —— 程序的进程号文件"),
        commands = listOf("ls /run", "ls -l /run/*.pid 2>/dev/null | head")
    )
)
