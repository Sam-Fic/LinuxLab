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

import com.linuxlab.starter.model.Danger
import com.linuxlab.starter.model.Faq
import com.linuxlab.starter.model.FaqCategory
import com.linuxlab.starter.model.FaqStep

/** 常见问题分类 */
val FaqCategories: List<FaqCategory> = listOf(
    FaqCategory("boot", "启动与系统", "Boot & System", "computer"),
    FaqCategory("package", "软件安装", "Packages", "package"),
    FaqCategory("disk", "磁盘与文件", "Disk & Files", "storage"),
    FaqCategory("perm", "权限与用户", "Permissions", "lock"),
    FaqCategory("network", "网络与 SSH", "Network & SSH", "language"),
    FaqCategory("process", "进程与性能", "Process & Perf", "memory"),
    FaqCategory("shell", "Shell 与脚本", "Shell & Scripts", "code"),
    FaqCategory("error", "报错速查", "Error Lookup", "search")
)

/** Linux 常见问题与处理办法 */
val Faqs: List<Faq> = listOf(
    // ---------------- 启动与系统 ----------------
    Faq(
        id = "boot-root-password",
        categoryId = "boot",
        title = "忘记 root 密码",
        symptom = "无法登录，也不知道 root 密码",
        cause = "密码遗矢，但只要能接触物理机/控制台就可以重置。",
        steps = listOf(
            FaqStep("重启，在 GRUB 菜单选中内核按 e 进入编辑"),
            FaqStep("在 linux/linuxefi 行末尾加上 init=/bin/bash（CentOS/RHEL 用 rd.break），按 Ctrl+X 启动"),
            FaqStep("重新挂载根目录为可写并改密码", "mount -o remount,rw / && passwd"),
            FaqStep("若开了 SELinux，强制刷新标签后重启", "touch /.autorelabel && exec /sbin/init")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "boot-start-job",
        categoryId = "boot",
        title = "开机卡在 “A start job is running for …”",
        symptom = "启动时长时间等待，最后进入 emergency mode",
        cause = "某个 systemd 单元超时，最常见是 /etc/fstab 里的网络盘或坏盘挂载不上。",
        steps = listOf(
            FaqStep("查看正在等待的任务", "systemctl list-jobs"),
            FaqStep("检查 /etc/fstab 里可疑的挂载项，给不需要开机挂载的加 nofail", "cat /etc/fstab"),
            FaqStep("临时屏蔽拖慢启动的服务", "systemctl mask systemd-networkd-wait-online.service"),
            FaqStep("看本次启动的报错", "journalctl -xb -p err")
        )
    ),
    Faq(
        id = "boot-fstab-broken",
        categoryId = "boot",
        title = "改坏 /etc/fstab 后无法启动",
        symptom = "启动时报 “You are in emergency mode” 或 fsck 失败",
        cause = "fstab 中有写错的 UUID、目录不存在或文件系统类型错误。",
        steps = listOf(
            FaqStep("输入 root 密码进入 emergency shell"),
            FaqStep("根目录重新挂载为可写", "mount -o remount,rw /"),
            FaqStep("先用 blkid 确认分区真实 UUID", "blkid"),
            FaqStep("修正或先注释掉出错的行，再重启", "vi /etc/fstab")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "boot-grub-lost",
        categoryId = "boot",
        title = "GRUB 损坏 / 重装 Windows 后进不了 Linux",
        symptom = "开机直接进 Windows 或出现 grub rescue>",
        cause = "引导记录被覆盖，需要用 LiveCD 重新安装 GRUB。",
        steps = listOf(
            FaqStep("用安装 U 盘进入试用(Live)模式，挂载原系统根分区", "mount /dev/sda2 /mnt"),
            FaqStep("挂载启动相关目录", "mount --bind /dev /mnt/dev && mount --bind /proc /mnt/proc && mount --bind /sys /mnt/sys"),
            FaqStep("chroot 进原系统", "chroot /mnt"),
            FaqStep("重装引导并生成配置", "grub-install /dev/sda && update-grub")
        ),
        danger = Danger.DANGEROUS
    ),
    Faq(
        id = "boot-timezone",
        categoryId = "boot",
        title = "系统时间或时区不对",
        symptom = "date 显示的时间差 8 小时或完全不对",
        cause = "时区设置错误，或没有开启时间同步。",
        steps = listOf(
            FaqStep("查看时间与同步状态", "timedatectl"),
            FaqStep("设为上海时区", "timedatectl set-timezone Asia/Shanghai"),
            FaqStep("开启网络对时", "timedatectl set-ntp true"),
            FaqStep("查看是否真的同步上", "chronyc sources -v")
        )
    ),
    Faq(
        id = "boot-shutdown-hang",
        categoryId = "boot",
        title = "关机 / 重启卡住不动",
        symptom = "执行 reboot 后长时间停在 “Reached target”",
        cause = "有服务拒绝退出（数据库、NFS、自定义脚本等）。",
        steps = listOf(
            FaqStep("另开终端查看卡住的任务", "systemctl list-jobs"),
            FaqStep("看本次关机相关日志", "journalctl -xb -p err"),
            FaqStep("不得已时强制重启（会跳过正常关闭流程）", "systemctl reboot -f"),
            FaqStep("也可缩短等待时间：编辑 /etc/systemd/system.conf 里的 DefaultTimeoutStopSec")
        ),
        danger = Danger.CAREFUL
    ),

    // ---------------- 软件安装 ----------------
    Faq(
        id = "pkg-lock",
        categoryId = "package",
        title = "Could not get lock /var/lib/dpkg/lock",
        symptom = "apt 安装时提示被另一个进程占用锁",
        cause = "软件中心/自动更新正在跑，或上次安装异常退出留下残留进程。",
        steps = listOf(
            FaqStep("确认是不是真有进程在跑", "ps aux | grep -E 'apt|dpkg' | grep -v grep"),
            FaqStep("确认无人安装后再动：先修复未完成的配置", "dpkg --configure -a"),
            FaqStep("仍报错才删除锁文件（确认无 apt 进程时）", "rm -f /var/lib/apt/lists/lock /var/lib/dpkg/lock-frontend"),
            FaqStep("最后更新索引", "apt update")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "pkg-broken-deps",
        categoryId = "package",
        title = "依赖关系破损装不上 / 卸不掉",
        symptom = "“有下列软件包未满足的依赖关系” 或 dpkg 报错",
        cause = "中断的安装、混用不同版本源导致依赖不一致。",
        steps = listOf(
            FaqStep("让 apt 自动修复依赖", "apt -f install"),
            FaqStep("把未配置完的包继续配完", "dpkg --configure -a"),
            FaqStep("查看破损包", "apt list --broken"),
            FaqStep("仍不行时用 aptitude 给出降级方案", "aptitude install <包名>")
        )
    ),
    Faq(
        id = "pkg-not-found",
        categoryId = "package",
        title = "Unable to locate package（找不到软件包）",
        symptom = "apt install 提示找不到包",
        cause = "索引太旧、名字写错，或该包在没启用的组件里。",
        steps = listOf(
            FaqStep("先刷新索引", "apt update"),
            FaqStep("按关键字搜索确认准确包名", "apt search nginx"),
            FaqStep("Ubuntu 启用 universe 组件", "add-apt-repository universe && apt update"),
            FaqStep("查看候选版本来源", "apt-cache policy nginx")
        )
    ),
    Faq(
        id = "pkg-slow-mirror",
        categoryId = "package",
        title = "下载源太慢 / 连接超时",
        symptom = "apt/yum/apk 卡在 0% 或 Err 超时",
        cause = "默认官方源在境外，换成国内镜像即可。",
        steps = listOf(
            FaqStep("Debian/Ubuntu 换源（清华镜像示例）", "sed -i 's|http://.*archive.ubuntu.com|https://mirrors.tuna.tsinghua.edu.cn|g' /etc/apt/sources.list && apt update"),
            FaqStep("Alpine 换源", "sed -i 's|dl-cdn.alpinelinux.org|mirrors.tuna.tsinghua.edu.cn|g' /etc/apk/repositories && apk update"),
            FaqStep("RHEL/CentOS 重建缓存", "dnf clean all && dnf makecache"),
            FaqStep("确认实际在从哪个地址下载", "apt-get update -o Debug::Acquire::http=true")
        )
    ),
    Faq(
        id = "pkg-gpg",
        categoryId = "package",
        title = "GPG error / NO_PUBKEY 密钥错误",
        symptom = "apt update 报 “The following signatures couldn't be verified”",
        cause = "第三方源的公钥没导入或已过期。",
        steps = listOf(
            FaqStep("按提示导入缺失的公钥（把 KEY 换成后 8 位）", "apt-key adv --keyserver keyserver.ubuntu.com --recv-keys ABCDEF12"),
            FaqStep("推荐的新做法：单独存 keyring", "curl -fsSL https://example.com/gpg | gpg --dearmor -o /etc/apt/keyrings/demo.gpg"),
            FaqStep("在源里用 signed-by 指向该 keyring", "deb [signed-by=/etc/apt/keyrings/demo.gpg] https://example.com/apt stable main")
        )
    ),
    Faq(
        id = "pkg-command-missing",
        categoryId = "package",
        title = "包装上了但命令还是找不到",
        symptom = "bash: xxx: command not found，但 dpkg 显示已安装",
        cause = "可执行文件不在 PATH 里，或包名与命令名不同。",
        steps = listOf(
            FaqStep("看命令在哪、PATH 有哪些目录", "which nginx; echo \$PATH"),
            FaqStep("列出包装了哪些文件", "dpkg -L nginx | grep bin"),
            FaqStep("按文件名反查属于哪个包", "dpkg -S /usr/sbin/nginx"),
            FaqStep("临时把目录加入 PATH", "export PATH=\$PATH:/usr/local/bin")
        )
    ),
    Faq(
        id = "pkg-purge",
        categoryId = "package",
        title = "卸载后残留配置和依赖",
        symptom = "重装后配置还是旧的，或磁盘上留一堆孤儿包",
        cause = "remove 只删程序，purge 才会连配置一起删。",
        steps = listOf(
            FaqStep("连配置一起卸载", "apt purge <包名>"),
            FaqStep("清理不再需要的依赖", "apt autoremove --purge"),
            FaqStep("清空下载缓存", "apt clean"),
            FaqStep("查看残留的 rc 状态包", "dpkg -l | grep '^rc'")
        )
    ),
    Faq(
        id = "pkg-hold-version",
        categoryId = "package",
        title = "需要装指定版本 / 防止被升级",
        symptom = "新版有 bug，想锁在旧版本",
        cause = "需要固定版本或禁止自动升级。",
        steps = listOf(
            FaqStep("查看可用版本", "apt-cache madison nginx"),
            FaqStep("安装指定版本", "apt install nginx=1.18.0-0ubuntu1"),
            FaqStep("锁定版本不被升级", "apt-mark hold nginx"),
            FaqStep("解锁", "apt-mark unhold nginx")
        )
    ),

    // ---------------- 磁盘与文件 ----------------
    Faq(
        id = "disk-full",
        categoryId = "disk",
        title = "磁盘满了：No space left on device",
        symptom = "写文件失败、服务崩溃，df 显示 100%",
        cause = "日志、缓存、备份或 core dump 占满分区。",
        steps = listOf(
            FaqStep("确认是哪个分区满", "df -h"),
            FaqStep("找出根目录下最大的子目录", "du -sh /* 2>/dev/null | sort -h | tail"),
            FaqStep("交互式逐层排查（最好用）", "ncdu /"),
            FaqStep("找最近 3 天内大于 100M 的文件", "find / -xdev -type f -mtime -3 -size +100M 2>/dev/null")
        )
    ),
    Faq(
        id = "disk-deleted-not-freed",
        categoryId = "disk",
        title = "删了大文件但空间没释放",
        symptom = "df 仍显示满，但 du 加起来对不上",
        cause = "文件被进程占用，删除后 inode 未真正释放。",
        steps = listOf(
            FaqStep("找出已被删除但仍被占用的文件", "lsof +L1"),
            FaqStep("也可以这样查", "lsof | grep deleted"),
            FaqStep("最稳妥：重启占用它的进程（而不是整台机器）", "systemctl restart <服务>"),
            FaqStep("应急：把还在写的日志清空（不删文件）", "truncate -s 0 /var/log/xxx.log")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "disk-inode",
        categoryId = "disk",
        title = "inode 用尽（空间还有却写不进）",
        symptom = "df -h 有空间，但提示 No space left on device",
        cause = "海量小文件（session、缓存、邮件队列）耗尽 inode。",
        steps = listOf(
            FaqStep("确认 inode 使用率", "df -i"),
            FaqStep("统计各目录下文件数量", "for d in /*; do echo \"\$(find \$d -xdev -type f 2>/dev/null | wc -l) \$d\"; done | sort -rn | head"),
            FaqStep("找到后清理小文件（确认目录！）", "find /var/spool/postfix/maildrop -type f -delete"),
            FaqStep("长期：改用能复用 inode 的存储方案或定期清理任务")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "disk-readonly",
        categoryId = "disk",
        title = "文件系统突然变成只读",
        symptom = "Read-only file system，写任何东西都失败",
        cause = "内核检测到磁盘错误或异常断电，为保护数据自动挂载为只读。",
        steps = listOf(
            FaqStep("看内核报错原因", "dmesg -T | tail -30"),
            FaqStep("尝试重新挂载为读写", "mount -o remount,rw /"),
            FaqStep("卸载后检查并修复（需 LiveCD 或卸载分区）", "fsck -y /dev/sdb1"),
            FaqStep("检查磁盘健康", "smartctl -a /dev/sda")
        ),
        danger = Danger.DANGEROUS
    ),
    Faq(
        id = "disk-mount-fail",
        categoryId = "disk",
        title = "挂载失败：unknown filesystem type / wrong fs type",
        symptom = "mount 报文件系统类型错误",
        cause = "没装对应驱动（NTFS/exFAT），或文件系统损坏、类型猜错。",
        steps = listOf(
            FaqStep("先确认它到底是什么文件系统", "blkid /dev/sdb1"),
            FaqStep("安装 NTFS/exFAT 支持", "apt install ntfs-3g exfat-fuse"),
            FaqStep("指定类型挂载", "mount -t ntfs-3g /dev/sdb1 /mnt"),
            FaqStep("坏盘先修复再挂", "fsck -y /dev/sdb1")
        )
    ),
    Faq(
        id = "disk-io-slow",
        categoryId = "disk",
        title = "磁盘 IO 很高，机器很卡",
        symptom = "负载高但 CPU 空闲，命令响应慢",
        cause = "某进程在疯狂读写（数据库、备份、日志）。",
        steps = listOf(
            FaqStep("看设备利用率（%util 接近 100 就是瓶颈）", "iostat -x 1"),
            FaqStep("找出谁在读写", "iotop -o"),
            FaqStep("没有 iotop 时看进程状态", "ps -eo pid,stat,comm | awk '\$2 ~ /D/'"),
            FaqStep("临时降低后台任务 IO 优先级", "ionice -c2 -n7 rsync -a /src /dst")
        )
    ),
    Faq(
        id = "disk-journal",
        categoryId = "disk",
        title = "systemd 日志撑爆磁盘",
        symptom = "/var/log/journal 好几个 G",
        cause = "journald 默认不限制体积，日志长期堆积。",
        steps = listOf(
            FaqStep("查看日志占用", "journalctl --disk-usage"),
            FaqStep("只保留最近 200M", "journalctl --vacuum-size=200M"),
            FaqStep("只保留最近 7 天", "journalctl --vacuum-time=7d"),
            FaqStep("永久限制：编辑 /etc/systemd/journald.conf 的 SystemMaxUse=200M 后重启服务", "systemctl restart systemd-journald")
        )
    ),
    Faq(
        id = "disk-lvm-extend",
        categoryId = "disk",
        title = "分区/LV 扩容后容量没变",
        symptom = "lvextend 成功但 df 显示没变大",
        cause = "扩了块设备，没扩文件系统。",
        steps = listOf(
            FaqStep("一步到位：扩逻辑卷的同时扩文件系统", "lvextend -r -L +10G /dev/mapper/vg0-root"),
            FaqStep("ext4 单独扩", "resize2fs /dev/mapper/vg0-root"),
            FaqStep("XFS 单独扩（注意 XFS 不能缩）", "xfs_growfs /"),
            FaqStep("确认结果", "df -h /")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "disk-rm-mistake",
        categoryId = "disk",
        title = "rm 误删了文件",
        symptom = "重要文件被 rm 掉",
        cause = "没有回收站机制，删除即解除链接。",
        steps = listOf(
            FaqStep("立刻停止对该分区的写入，最好马上只读重挂", "mount -o remount,ro /"),
            FaqStep("ext 文件系统尝试恢复", "extundelete /dev/sdb1 --restore-file /home/u/a.txt"),
            FaqStep("通用抢救（按文件类型）", "photorec /dev/sdb1"),
            FaqStep("以后预防：给 rm 加确认或用回收站工具", "alias rm='rm -i'  # 或 apt install trash-cli")
        ),
        danger = Danger.DANGEROUS
    ),

    // ---------------- 权限与用户 ----------------
    Faq(
        id = "perm-denied",
        categoryId = "perm",
        title = "Permission denied（权限不足）",
        symptom = "读/写/执行文件或进目录被拒",
        cause = "缺少对应权限位，或父目录没有执行(x)权限。",
        steps = listOf(
            FaqStep("看权限和属主", "ls -l 文件; ls -ld 目录"),
            FaqStep("看自己是谁、属于哪些组", "id"),
            FaqStep("补执行位（脚本/程序）", "chmod +x ./script.sh"),
            FaqStep("进目录必须有 x 权限，补上", "chmod +x /home/user")
        )
    ),
    Faq(
        id = "perm-sudoers",
        categoryId = "perm",
        title = "xxx is not in the sudoers file",
        symptom = "用 sudo 时提示不在 sudoers 中",
        cause = "该用户没有被授权使用 sudo。",
        steps = listOf(
            FaqStep("用 root 把用户加入 sudo 组（Debian/Ubuntu）", "usermod -aG sudo alice"),
            FaqStep("RHEL/CentOS 是 wheel 组", "usermod -aG wheel alice"),
            FaqStep("或单独授权（用 visudo，别直接 vi！）", "visudo"),
            FaqStep("在文件中加一行", "alice ALL=(ALL) NOPASSWD:ALL")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "perm-chmod-disaster",
        categoryId = "perm",
        title = "误执行 chmod -R 777 / 后系统异常",
        symptom = "sudo 报 suid 警告、ssh 登录失败、各种服务起不来",
        cause = "关键文件权限被破坏（ssh 私钥、shadow、suid 程序都不能是 777）。",
        steps = listOf(
            FaqStep("RHEL/CentOS：按 RPM 数据库重置权限", "rpm --setperms -a && rpm --setugids -a"),
            FaqStep("Debian/Ubuntu：重装全量包（较慢）", "apt install --reinstall \$(dpkg -S /usr/bin /usr/sbin 2>/dev/null | cut -d: -f1 | sort -u)"),
            FaqStep("修 SSH 私钥权限", "chmod 600 /etc/ssh/ssh_host_*_key && chmod 644 /etc/ssh/ssh_host_*_key.pub"),
            FaqStep("教训：永远不要对 / 递归 chmod，先 chmod 单个目录")
        ),
        danger = Danger.DANGEROUS
    ),
    Faq(
        id = "perm-ssh-key",
        categoryId = "perm",
        title = "SSH 私钥 “Permissions 0644 are too open”",
        symptom = "ssh 拒绝使用私钥，WARNING: UNPROTECTED PRIVATE KEY FILE",
        cause = "私钥或 .ssh 目录权限太宽，SSH 出于安全拒绝使用。",
        steps = listOf(
            FaqStep("私钥必须 600", "chmod 600 ~/.ssh/id_rsa"),
            FaqStep("目录必须 700", "chmod 700 ~/.ssh"),
            FaqStep("公钥与 known_hosts 用 644", "chmod 644 ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys"),
            FaqStep("属主也要是自己", "chown -R \$USER:\$USER ~/.ssh")
        )
    ),
    Faq(
        id = "perm-owner",
        categoryId = "perm",
        title = "文件属主/属组不对",
        symptom = "root 建的文件，普通用户改不了",
        cause = "属主不属于当前用户。",
        steps = listOf(
            FaqStep("查看属主", "ls -l"),
            FaqStep("改属主和属组", "chown alice:alice file.txt"),
            FaqStep("递归修改目录", "chown -R www-data:www-data /var/www/html"),
            FaqStep("只改属组", "chgrp -R docker /var/run/docker.sock")
        )
    ),
    Faq(
        id = "perm-quota-readonly",
        categoryId = "perm",
        title = "有权限却写不进（只读挂载/配额）",
        symptom = "权限看着没问题，但仍写失败",
        cause = "分区以 ro 挂载、磁盘配额用尽或文件被 chattr 锁定。",
        steps = listOf(
            FaqStep("看挂载选项", "mount | grep ' / '"),
            FaqStep("看配额", "quota -s"),
            FaqStep("看文件是否被加了不可变属性", "lsattr 文件"),
            FaqStep("解锁不可变属性", "chattr -i 文件")
        )
    ),

    // ---------------- 网络与 SSH ----------------
    Faq(
        id = "net-dns",
        categoryId = "network",
        title = "ping: unknown host（域名解析失败）",
        symptom = "能 ping 通 IP 但 ping 不通域名",
        cause = "/etc/resolv.conf 里没有可用的 DNS。",
        steps = listOf(
            FaqStep("看当前 DNS 配置", "cat /etc/resolv.conf"),
            FaqStep("用指定 DNS 测试解析", "nslookup baidu.com 223.5.5.5"),
            FaqStep("临时写入公共 DNS", "echo 'nameserver 223.5.5.5' >> /etc/resolv.conf"),
            FaqStep("systemd-resolved 环境用这个看/改", "resolvectl status")
        )
    ),
    Faq(
        id = "net-port-used",
        categoryId = "network",
        title = "Address already in use（端口被占用）",
        symptom = "服务启动失败，提示端口已被占用",
        cause = "另一个进程已经监听了该端口。",
        steps = listOf(
            FaqStep("看谁占了 80 端口", "ss -lntp | grep ':80'"),
            FaqStep("或用 lsof", "lsof -i:80"),
            FaqStep("确认后停掉它", "systemctl stop nginx"),
            FaqStep("不想停就改自己服务的监听端口")
        )
    ),
    Faq(
        id = "net-listen-local",
        categoryId = "network",
        title = "服务只监听 127.0.0.1，外部连不上",
        symptom = "本机 curl 通，别的机器连不上",
        cause = "服务绑定在回环地址，或被防火墙拦了。",
        steps = listOf(
            FaqStep("确认监听地址（0.0.0.0 才是对外）", "ss -lntp"),
            FaqStep("修改服务配置里的 bind/address 为 0.0.0.0 后重启", "systemctl restart <服务>"),
            FaqStep("放通防火墙（Ubuntu）", "ufw allow 80/tcp"),
            FaqStep("放通防火墙（RHEL）", "firewall-cmd --add-port=80/tcp --permanent && firewall-cmd --reload")
        )
    ),
    Faq(
        id = "net-ssh-timeout",
        categoryId = "network",
        title = "SSH 连不上：超时 / Connection refused",
        symptom = "ssh 卡住无响应，或立刻被拒绝",
        cause = "超时多为防火墙/安全组丢包；refused 是服务没监听。",
        steps = listOf(
            FaqStep("先看服务端 sshd 是否在跑", "systemctl status sshd"),
            FaqStep("确认端口在监听", "ss -lntp | grep ssh"),
            FaqStep("客户端开详细日志定位卡在哪一步", "ssh -vvv user@host -p 22"),
            FaqStep("放通端口后再试", "ufw allow 22/tcp")
        )
    ),
    Faq(
        id = "net-ssh-slow",
        categoryId = "network",
        title = "SSH 登录要等几十秒才出密码提示",
        symptom = "连接很慢但连上后正常",
        cause = "服务端在做 DNS 反查或 GSSAPI 认证。",
        steps = listOf(
            FaqStep("在 /etc/ssh/sshd_config 里加两行", "UseDNS no\nGSSAPIAuthentication no"),
            FaqStep("检查语法", "sshd -t"),
            FaqStep("重启 sshd", "systemctl restart sshd"),
            FaqStep("客户端侧也可禁用", "ssh -o GSSAPIAuthentication=no user@host")
        )
    ),
    Faq(
        id = "net-ip-notapply",
        categoryId = "network",
        title = "改了 IP 但不生效 / 重启后还原",
        symptom = "ip addr 改完又变回去",
        cause = "ip 命令是临时的，需写进发行版的网络配置。",
        steps = listOf(
            FaqStep("临时改 IP（重启即失效）", "ip addr add 192.168.1.10/24 dev eth0"),
            FaqStep("Ubuntu netplan 持久化后应用", "netplan apply"),
            FaqStep("RHEL/CentOS NetworkManager 重新加载", "nmcli con reload && nmcli con up eth0"),
            FaqStep("确认路由和默认网关", "ip route")
        )
    ),
    Faq(
        id = "net-cert",
        categoryId = "network",
        title = "curl 报证书错误 SSL certificate problem",
        symptom = "curl/wget 提示证书不可信或已过期",
        cause = "本机 CA 证书过旧，或系统时间不对，或中间人代理。",
        steps = listOf(
            FaqStep("先确认系统时间对不对（非常常见）", "date"),
            FaqStep("看证书链详情", "openssl s_client -connect example.com:443 -servername example.com"),
            FaqStep("更新根证书", "apt install --reinstall ca-certificates && update-ca-certificates"),
            FaqStep("临时跳过校验（仅测试用，不安全）", "curl -k https://example.com")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "net-slow",
        categoryId = "network",
        title = "网络慢、丢包",
        symptom = "下载慢、SSH 卡顿、时断时续",
        cause = "链路丢包、MTU 不匹配、带宽被占满。",
        steps = listOf(
            FaqStep("先看丢包率", "ping -c 100 8.8.8.8"),
            FaqStep("定位哪一跳开始丢（比 traceroute 好用）", "mtr -rw 8.8.8.8"),
            FaqStep("看连接统计和重传", "ss -s"),
            FaqStep("PPPoE/隧道环境尝试调小 MTU", "ip link set dev eth0 mtu 1400")
        )
    ),

    // ---------------- 进程与性能 ----------------
    Faq(
        id = "proc-high-load",
        categoryId = "process",
        title = "负载飙高，机器变卡",
        symptom = "load average 几十，操作明显延迟",
        cause = "CPU 密集进程、大量 IO 等待或进程数过多。",
        steps = listOf(
            FaqStep("看整体负载和趋势", "uptime"),
            FaqStep("实时看谁在吃资源", "top"),
            FaqStep("按 CPU 排序取前几名", "ps -eo pid,ppid,pcpu,pmem,comm --sort=-pcpu | head"),
            FaqStep("区分是 CPU 还是 IO：%wa 高说明在等磁盘", "vmstat 1")
        )
    ),
    Faq(
        id = "proc-oom",
        categoryId = "process",
        title = "内存不足被 OOM 杀掉",
        symptom = "进程突然消失，日志出现 Out of memory: Killed process",
        cause = "内存耗尽，内核按评分杀掉最“大”的进程。",
        steps = listOf(
            FaqStep("确认内存和 swap 使用", "free -h"),
            FaqStep("查历史 OOM 记录", "journalctl -k | grep -i 'killed process'"),
            FaqStep("找出最吃内存的进程", "ps -eo pid,ppid,pmem,rss,comm --sort=-rss | head"),
            FaqStep("临时缓解：降低换页倾向（0~100，越小越少用 swap）", "sysctl vm.swappiness=10")
        )
    ),
    Faq(
        id = "proc-cannot-kill",
        categoryId = "process",
        title = "kill 杀不掉进程",
        symptom = "kill 之后进程还在",
        cause = "忽略了信号、处于不可中断睡眠(D)，或已经是僵尸。",
        steps = listOf(
            FaqStep("先尝试优雅退出（15）", "kill -15 <PID>"),
            FaqStep("再强制（9）", "kill -9 <PID>"),
            FaqStep("看进程状态：D 是在等 IO，Z 是僵尸", "ps -o pid,stat,wchan:30,comm -p <PID>"),
            FaqStep("僵尸进程杀不掉，要杀它的父进程", "ps -eo pid,ppid,stat,comm | awk '\$3 ~ /Z/'")
        ),
        danger = Danger.CAREFUL
    ),
    Faq(
        id = "proc-zombie",
        categoryId = "process",
        title = "出现僵尸进程（defunct）",
        symptom = "ps 里出现 <defunct>，状态为 Z",
        cause = "父进程没有 wait() 回收子进程，僵尸本身不占资源。",
        steps = listOf(
            FaqStep("列出僵尸及其父进程", "ps -eo pid,ppid,stat,comm | grep -w Z"),
            FaqStep("重启或通知父进程回收（无法直接杀死僵尸）", "kill -1 <PPID>"),
            FaqStep("父进程本身有 bug 就重启父进程", "systemctl restart <服务>"),
            FaqStep("数量少且父进程是 init 会自动回收，可忽略")
        )
    ),
    Faq(
        id = "proc-cpu-thread",
        categoryId = "process",
        title = "CPU 100%，定位到具体线程",
        symptom = "某个进程 CPU 打满",
        cause = "死循环、GC 频繁、正则回溯等。",
        steps = listOf(
            FaqStep("看进程内各线程的 CPU", "top -H -p <PID>"),
            FaqStep("把线程 ID 转成十六进制（用于对照堆栈）", "printf '%x\\n' <TID>"),
            FaqStep("Java 程序抓线程栈", "jstack <PID> | grep -A 20 'nid=0x<十六进制TID>'"),
            FaqStep("通用：跟踪系统调用看卡在哪", "strace -p <PID> -tt -T")
        )
    ),
    Faq(
        id = "proc-background",
        categoryId = "process",
        title = "退出终端后后台任务被杀掉",
        symptom = "断开 SSH，跑的任务就没了",
        cause = "会话结束会向进程组发 SIGHUP。",
        steps = listOf(
            FaqStep("简单后台且忽略挂断", "nohup ./run.sh > run.log 2>&1 &"),
            FaqStep("脱离终端会话", "setsid ./run.sh &"),
            FaqStep("可随时回去查看（推荐）", "tmux new -s work   # 断开后 tmux attach -t work"),
            FaqStep("已经启动的作业移出作业表", "disown -h %1")
        )
    ),
    Faq(
        id = "proc-too-many-files",
        categoryId = "process",
        title = "Too many open files（文件句柄耗尽）",
        symptom = "服务报错无法打开新文件/连接",
        cause = "进程打开文件数超过 ulimit 限制，或句柄泄漏。",
        steps = listOf(
            FaqStep("看当前限制", "ulimit -n"),
            FaqStep("统计某进程打开了多少文件", "lsof -p <PID> | wc -l"),
            FaqStep("看系统总量", "cat /proc/sys/fs/file-nr"),
            FaqStep("永久放宽：在 /etc/security/limits.conf 加两行（* soft nofile 65535 / * hard nofile 65535），重新登录生效")
        )
    ),

    // ---------------- Shell 与脚本 ----------------
    Faq(
        id = "shell-not-found-but-exists",
        categoryId = "shell",
        title = "command not found 但文件明明存在",
        symptom = "./xxx 报 command not found 或 No such file or directory",
        cause = "PATH 不含该目录；或脚本 shebang 指向不存在的解释器；或架构/动态库不匹配。",
        steps = listOf(
            FaqStep("用绝对路径直接执行试试", "/usr/local/bin/xxx"),
            FaqStep("没有执行权限就加上", "chmod +x ./xxx && ./xxx"),
            FaqStep("看是什么类型的文件（架构是否匹配）", "file ./xxx"),
            FaqStep("看依赖的动态库是否齐全（出现 not found 就是缺库）", "ldd ./xxx")
        )
    ),
    Faq(
        id = "shell-crlf",
        categoryId = "shell",
        title = "脚本报 /bin/bash^M: bad interpreter",
        symptom = "Windows 编辑过的脚本在 Linux 上跑不起来",
        cause = "换行符是 CRLF，^M 被当成解释器名的一部分。",
        steps = listOf(
            FaqStep("转换换行符", "dos2unix script.sh"),
            FaqStep("没有 dos2unix 就用 sed", "sed -i 's/\\r\$//' script.sh"),
            FaqStep("确认已无 CR", "file script.sh"),
            FaqStep("加执行权限再跑", "chmod +x script.sh && ./script.sh")
        )
    ),
    Faq(
        id = "shell-garbled",
        categoryId = "shell",
        title = "中文显示乱码",
        symptom = "文件名或输出是 ??? 或方框",
        cause = "系统 locale 不是 UTF-8，或终端编码不匹配。",
        steps = listOf(
            FaqStep("查看当前语言环境", "locale"),
            FaqStep("列出已生成的 locale", "locale -a"),
            FaqStep("生成并切换到 UTF-8", "locale-gen zh_CN.UTF-8 && update-locale LANG=zh_CN.UTF-8"),
            FaqStep("临时生效", "export LANG=C.UTF-8")
        )
    ),
    Faq(
        id = "shell-sudo-redirect",
        categoryId = "shell",
        title = "sudo echo > 文件时 Permission denied",
        symptom = "sudo 了仍然写不进 /etc 下的文件",
        cause = "重定向发生在 sudo 之前，用的是当前用户的权限。",
        steps = listOf(
            FaqStep("正确做法一：整体包一层 shell", "sudo sh -c 'echo vm.swappiness=10 > /etc/sysctl.d/99-tune.conf'"),
            FaqStep("正确做法二：用 tee", "echo 'vm.swappines=10' | sudo tee /etc/sysctl.d/99-tune.conf"),
            FaqStep("追加用 tee -a", "echo 'nameserver 8.8.8.8' | sudo tee -a /etc/resolv.conf"),
            FaqStep("再让配置生效", "sudo sysctl --system")
        )
    ),
    Faq(
        id = "shell-pipe-quirk",
        categoryId = "shell",
        title = "管道后面拿不到结果 / 变量值丢失",
        symptom = "cmd | while read line 里赋的值出了循环就没了",
        cause = "管道会开子 shell，子 shell 里的变量不会传回父 shell。",
        steps = listOf(
            FaqStep("避免子 shell：用重定向喂给 while", "while read -r line; do n=\$line; done < file"),
            FaqStep("或用进程替换", "while read -r line; do echo \$line; done < <(cat file)"),
            FaqStep("grep 没输出时先看是不是走到了 stderr", "cmd 2>&1 | grep key"),
            FaqStep("调试脚本：逐行回显", "bash -x script.sh")
        )
    ),
    Faq(
        id = "shell-completion",
        categoryId = "shell",
        title = "Tab 补全不好使 / 命令历史找不到",
        symptom = "按 Tab 没有补全，history 里没有刚执行的命令",
        cause = "没装 bash-completion；历史在退出时才从内存写入文件。",
        steps = listOf(
            FaqStep("安装补全支持", "apt install bash-completion"),
            FaqStep("当前会话立即生效", "source /etc/bash_completion"),
            FaqStep("立刻把内存里的历史写入文件", "history -a"),
            FaqStep("反向搜索历史：按 Ctrl+R 后输入关键字")
        )
    ),
    Faq(
        id = "shell-history-tune",
        categoryId = "shell",
        title = "想让历史记录更好用",
        symptom = "history 条数太少、没有时间戳、重复命令太多",
        cause = "默认配置较简陋，可在 ~/.bashrc 里定制。",
        steps = listOf(
            FaqStep("常用配置（写入 ~/.bashrc）", "echo 'HISTSIZE=20000\\nHISTFILESIZE=20000\\nHISTTIMEFORMAT=\"%F %T \"\\nHISTCONTROL=ignoredups:erasedups\\nshopt -s histappend' >> ~/.bashrc"),
            FaqStep("立即生效", "source ~/.bashrc"),
            FaqStep("带时间戳查看", "history"),
            FaqStep("忽略以空格开头的命令（加 HISTCONTROL=ignorespace）")
        )
    ),

    // ---------------- 报错速查 ----------------
    Faq(
        id = "err-no-such-file",
        categoryId = "error",
        title = "No such file or directory，但 ls 明明能看到",
        symptom = "执行/打开文件时报文件不存在",
        cause = "脚本 shebang 的解释器不存在；软链接指向失效目标；缺 32 位库。",
        steps = listOf(
            FaqStep("确认软链接是否指向有效目标", "readlink -f 文件"),
            FaqStep("看第一行 shebang 的解释器是否存在", "head -1 脚本; which bash"),
            FaqStep("看是不是缺动态库", "ldd 文件 | grep 'not found'"),
            FaqStep("64 位系统跑 32 位程序需装运行时", "apt install libc6-i386")
        )
    ),
    Faq(
        id = "err-operation-not-permitted",
        categoryId = "error",
        title = "root 下也 Operation not permitted",
        symptom = "连删除/修改文件都被拒",
        cause = "文件被 chattr 加了不可变属性，或容器缺少对应 capability。",
        steps = listOf(
            FaqStep("查看特殊属性（i 表示不可变，a 表示只能追加）", "lsattr 文件"),
            FaqStep("去掉不可变属性", "chattr -i 文件"),
            FaqStep("去掉只追加属性", "chattr -a 文件"),
            FaqStep("容器里还需给相应 capability 或加 --privileged")
        )
    ),
    Faq(
        id = "err-text-file-busy",
        categoryId = "error",
        title = "Text file busy（文本文件忙）",
        symptom = "覆盖一个正在运行的程序时报忙",
        cause = "目标文件正在被执行，内核禁止写入。",
        steps = listOf(
            FaqStep("先停掉占用它的进程", "fuser -v /usr/bin/xxx; systemctl stop xxx"),
            FaqStep("正确做法：先删再装，让新文件用新 inode", "rm -f /usr/bin/xxx && install -m 755 xxx /usr/bin/xxx"),
            FaqStep("或解压到临时文件后 mv 覆盖（原子替换）", "cp new /usr/bin/xxx.tmp && mv -f /usr/bin/xxx.tmp /usr/bin/xxx")
        )
    ),
    Faq(
        id = "err-segfault",
        categoryId = "error",
        title = "Segmentation fault (core dumped)",
        symptom = "程序一运行就崩",
        cause = "程序 bug、内存越界、库版本冲突或编译产物与运行环境不匹配。",
        steps = listOf(
            FaqStep("看内核记录的崩溃信息", "dmesg -T | tail -20"),
            FaqStep("允许生成 core 文件", "ulimit -c unlimited"),
            FaqStep("用 gdb 看崩溃点", "gdb /usr/bin/xxx core"),
            FaqStep("排查库冲突", "ldd /usr/bin/xxx")
        )
    ),
    Faq(
        id = "err-broken-pipe",
        categoryId = "error",
        title = "Broken pipe / write error",
        symptom = "管道命令结尾报 broken pipe",
        cause = "下游命令（如 head）提前退出，上游还在写。",
        steps = listOf(
            FaqStep("多数情况下无害，可忽略"),
            FaqStep("脚本里想避免噪音可关闭 SIGPIPE 报错", "trap '' PIPE"),
            FaqStep("需要严格判断管道失败时开启 pipefail", "set -o pipefail"),
            FaqStep("例：只取前 10 行一般会触发该信息", "dmesg | head -10")
        )
    ),
    Faq(
        id = "err-fork",
        categoryId = "error",
        title = "Resource temporarily unavailable / cannot fork",
        symptom = "无法启动新进程、new thread 失败",
        cause = "进程数/线程数达到 ulimit 或系统上限。",
        steps = listOf(
            FaqStep("看当前用户进程数限制", "ulimit -u"),
            FaqStep("统计当前进程/线程数", "ps -eLf | wc -l"),
            FaqStep("看系统总量上限", "cat /proc/sys/kernel/threads-max"),
            FaqStep("临时放大", "ulimit -u 8192")
        )
    ),
    Faq(
        id = "err-conn-refused-vs-timeout",
        categoryId = "error",
        title = "Connection refused 与 timed out 的区别",
        symptom = "连不上服务，两种报错怎么区分",
        cause = "refused 是端口没人监听（服务没起）；timeout 是包被防火墙/安全组丢弃。",
        steps = listOf(
            FaqStep("先看本机服务是否在监听", "ss -lntp | grep <端口>"),
            FaqStep("从本机试探端口连通性", "nc -vz 127.0.0.1 8080"),
            FaqStep("看防火墙规则", "iptables -L -n | grep <端口>"),
            FaqStep("云服务器还要检查安全组/ACL")
        )
    ),
    Faq(
        id = "err-disk-quota",
        categoryId = "error",
        title = "Disk quota exceeded",
        symptom = "有空间但写不进，报配额超限",
        cause = "该用户/组的磁盘配额用尽。",
        steps = listOf(
            FaqStep("看自己的配额", "quota -s"),
            FaqStep("管理员查看全部分区配额", "repquota -a"),
            FaqStep("清理文件后重新检查", "du -sh ~/* | sort -h | tail"),
            FaqStep("调整配额（ext4/xfs 工具不同）", "setquota -u alice 5G 6G 0 0 /")
        )
    )
)
