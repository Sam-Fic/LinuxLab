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

/** 磁盘与存储 */
val DiskGroup = group("disk", "磁盘与存储", "Disks & Storage", "storage") {

    c("df", "查看磁盘空间占用", "Report filesystem disk space usage", "df [选项] [文件]") {
        detail("看分区还剩多少空间；磁盘满导致服务异常时第一个要查的命令。", "Shows free space per filesystem — the first check when a service fails because the disk is full.")
        p("-h", "以 KB/MB/GB 显示（推荐）", "Human-readable sizes")
        p("-T", "显示文件系统类型", "Show the filesystem type")
        p("-i", "查看 inode 使用情况", "Show inode usage instead of blocks")
        p("-t 类型", "只看某种文件系统", "Limit to a filesystem type")
        e("df -h", "查看所有分区的空间使用", "Show space usage for all mounts")
        e("df -i", "查看 inode 是否耗尽（小文件太多时）", "Check inode exhaustion")
        e("df -h /home", "只看 /home 分区", "Show usage for /home only")
        tip("空间没满但报 No space left on device，多半是 inode 用光了，用 df -i 确认。")
        rel("du", "lsblk", "mount", "ncdu")
    }

    c("du", "查看目录占用空间", "Estimate file and directory space usage", "du [选项] [路径]") {
        detail("统计每个目录占了多少磁盘，用来找出「大胃王」目录。", "Measures how much space directories take — how you find the space hogs.")
        p("-h", "以人类可读单位显示", "Human-readable sizes")
        p("-s", "只显示总计", "Show only a total for each argument")
        p("-d N", "只统计 N 层深度", "Show totals N levels deep")
        p("-a", "同时显示文件（不只有目录）", "Show files as well as directories")
        p("--max-depth=N", "同 -d N", "Same as -d N")
        e("du -sh *", "查看当前目录各子项占用大小", "Show the size of everything here")
        e("du -h -d 1 /var | sort -h", "按大小排序查看 /var 下第一层目录", "Show /var subdirectories sorted by size")
        e("du -ah /home | sort -rh | head -20", "找出最大的 20 个文件或目录", "Find the 20 largest items under /home")
        tip("想交互式查找大文件，可以安装 ncdu：ncdu /")
        rel("df", "ncdu", "ls", "find")
    }

    c("mount", "挂载文件系统", "Mount a filesystem", "mount [选项] 设备 挂载点", level = Level.ADVANCED) {
        detail("把磁盘分区、U 盘、网络存储挂到目录树上才能访问；/etc/fstab 定义开机自动挂载。", "Disks, USB drives and network shares must be mounted onto the directory tree; /etc/fstab defines what mounts at boot.")
        p("-t 类型", "指定文件系统类型，如 ext4 / ntfs", "Set the filesystem type")
        p("-o 选项", "挂载选项，如 ro（只读）、noatime", "Mount options such as ro or noatime")
        p("-a", "挂载 /etc/fstab 里配置的所有项", "Mount everything in /etc/fstab")
        p("-l", "列出已挂载项并显示标签", "List mounted filesystems with labels")
        e("mount | column -t", "整齐查看已挂载的文件系统", "List mounts in a table")
        e("sudo mount /dev/sdb1 /mnt/usb", "把 U 盘分区挂载到 /mnt/usb", "Mount a USB partition")
        e("sudo mount -o ro /dev/sdb1 /mnt", "以只读方式挂载（取证场景）", "Mount read-only")
        rel("umount", "lsblk", "df", "blkid")
    }

    c("umount", "卸载文件系统", "Unmount a filesystem", "umount [选项] 设备或挂载点") {
        p("-l", "延迟卸载（设备忙时）", "Lazy unmount when the device is busy")
        p("-f", "强制卸载（网络存储慎用）", "Force unmount (use with care)")
        e("sudo umount /mnt/usb", "卸载 U 盘", "Unmount a USB drive")
        e("sudo umount -l /mnt/usb", "提示 device is busy 时延迟卸载", "Lazy unmount when busy")
        tip("卸载前先 cd 出挂载目录，否则会报 target is busy。")
        rel("mount", "lsblk", "lsof")
    }

    c("blkid", "查看分区的 UUID 与类型", "Locate and print block device attributes", "blkid [选项] [设备]", level = Level.ADVANCED) {
        detail("配置 /etc/fstab 时用 UUID 比 /dev/sdb1 稳定，因为设备名可能变化。", "UUIDs in /etc/fstab are safer than /dev/sdX names, which can change between boots.")
        p("-o list", "以表格形式输出", "Print a table")
        e("sudo blkid", "列出所有分区的 UUID 与文件系统类型", "Show UUIDs and filesystem types")
        e("sudo blkid /dev/sdb1", "查看单个分区信息", "Show one partition")
        rel("lsblk", "mount", "fdisk")
    }

    c("fdisk", "查看与管理磁盘分区表", "Manipulate the disk partition table", "fdisk [选项] 设备", level = Level.ADVANCED) {
        detail("交互式分区工具；操作前务必确认磁盘，写错盘会丢数据。", "An interactive partitioning tool — double-check the device, mistakes destroy data.")
        p("-l", "列出所有磁盘分区（安全只读）", "List partition tables (read-only, safe)")
        p("-u", "以扇区为单位显示", "Show sizes in sectors")
        e("sudo fdisk -l", "查看所有磁盘和分区", "List all disks and partitions")
        e("sudo fdisk /dev/sdb", "对 /dev/sdb 进行分区操作", "Partition /dev/sdb")
        tip("分区前先备份。大于 2TB 的磁盘请使用 parted 或 GPT 分区表。")
        rel("parted", "lsblk", "mkfs")
    }

    c("parted", "GPT 分区与大磁盘管理", "A partition manipulation program", "parted [选项] 设备 [命令]", level = Level.ADVANCED) {
        p("-l", "列出所有磁盘的分区信息", "List partition layout of all devices")
        p("mklabel gpt", "创建 GPT 分区表", "Create a GPT label")
        p("print", "显示分区表", "Show the partition table")
        e("sudo parted -l", "查看磁盘容量与分区方案", "Show disks and their partition tables")
        e("sudo parted /dev/sdb mklabel gpt", "把磁盘初始化为 GPT", "Initialise a disk with GPT")
        rel("fdisk", "mkfs", "lsblk")
    }

    c("mkfs", "格式化分区（会清空数据）", "Build a Linux filesystem", "mkfs [选项] 设备", level = Level.ADVANCED) {
        detail("创建文件系统，等同于「格式化」，执行后数据全部丢失。", "Creates a filesystem — i.e. formats the device. All data is lost.")
        p("-t 类型", "指定文件系统，如 ext4 / xfs / vfat", "Set the filesystem type")
        p("-L 标签", "设置卷标", "Set the volume label")
        p("-c", "创建前检查坏块（很慢）", "Check for bad blocks (slow)")
        e("sudo mkfs.ext4 /dev/sdb1", "把分区格式化为 ext4", "Format a partition as ext4")
        e("sudo mkfs.vfat -F 32 /dev/sdb1", "格式化为 FAT32（U 盘通用）", "Format as FAT32 for USB drives")
        e("sudo mkfs.ext4 -L data /dev/sdb1", "格式化并设置卷标 data", "Format with a label")
        tip("命令也常写作 mkfs.ext4 / mkfs.xfs / mkfs.vfat 这样的别名形式。")
        rel("fdisk", "mount", "fsck", "parted")
    }

    c("fsck", "检查并修复文件系统", "Check and repair a filesystem", "fsck [选项] 设备", level = Level.ADVANCED) {
        detail("必须在卸载状态下运行，否则可能损坏数据。", "Always run it on an unmounted filesystem, otherwise you may corrupt data.")
        p("-y", "自动回答 yes 修复", "Answer yes to all repair questions")
        p("-f", "强制检查（即使看起来正常）", "Force a check")
        p("-n", "只检查不修复", "Check only, do not change anything")
        e("sudo fsck -f /dev/sdb1", "强制检查分区", "Force a filesystem check")
        e("sudo fsck -y /dev/sdb1", "自动修复发现的问题", "Automatically repair problems")
        tip("系统启动时若提示 fsck，通常是异常断电后的自动检查，耐心等它跑完。")
        rel("mkfs", "mount", "dmesg", "e2fsck")
    }

    c("dd", "按字节复制数据", "Convert and copy a file byte by byte", "dd if=输入 of=输出 [选项]", level = Level.ADVANCED) {
        detail("可用来制作启动盘、备份整块磁盘、生成测试文件；参数写错会毁掉数据，务必核对 of=。", "Great for bootable USBs, disk images and test files — but a wrong of= destroys data, so double-check it.")
        p("if=", "输入文件（源文件）", "Input file")
        p("of=", "输出文件（目标）", "Output file")
        p("bs=", "块大小，如 4M 提高速度", "Block size (4M is faster)")
        p("count=", "只复制指定数量的块", "Copy only N blocks")
        p("status=progress", "显示进度", "Show progress")
        e("sudo dd if=ubuntu.iso of=/dev/sdb bs=4M status=progress && sync", "把 ISO 写入 U 盘制作启动盘", "Write an ISO to a USB stick")
        e("dd if=/dev/zero of=test.bin bs=1M count=100", "生成 100MB 测试文件", "Create a 100 MB test file")
        e("sudo dd if=/dev/sda of=disk.img bs=4M status=progress", "整盘镜像备份", "Image an entire disk")
        tip("dd 因为风险高被戏称为 disk destroyer，敲回车前再看一遍 of= 参数。")
        rel("cp", "sync", "shred", "lsblk")
    }

    c("sync", "把缓存中的数据写入磁盘", "Flush filesystem buffers to disk", "sync [选项]") {
        detail("拔 U 盘前先 sync，确保所有数据真正落盘。", "Run sync before unplugging a drive so every buffer reaches the disk.")
        e("sync", "强制把缓存写入磁盘", "Flush all pending writes")
        e("sync && sudo umount /mnt/usb", "安全卸载 U 盘的完整流程", "Safely unmount a USB drive")
        rel("dd", "umount", "mount")
    }

    c("hdparm", "测试与设置硬盘参数", "Get or set SATA/IDE device parameters", "hdparm [选项] 设备", level = Level.ADVANCED) {
        p("-Tt", "测试磁盘缓存与读取速度", "Benchmark cached and buffered reads")
        p("-I", "显示硬盘详细信息", "Show drive identification info")
        e("sudo hdparm -Tt /dev/sda", "测试硬盘读取速度", "Measure disk read speed")
        e("sudo hdparm -I /dev/sda | head -20", "查看硬盘型号与特性", "Show drive details")
        rel("iostat", "dd", "lsblk")
    }

    c("iostat", "查看磁盘 IO 负载", "Report CPU and I/O statistics", "iostat [选项] [间隔]", level = Level.ADVANCED) {
        detail("定位磁盘瓶颈的利器：%util 接近 100% 说明磁盘已饱和。", "Finds I/O bottlenecks: %util near 100% means the disk is saturated.")
        p("-x", "显示扩展统计（含 await、%util）", "Show extended statistics")
        p("-d", "只显示设备统计", "Show only device reports")
        e("iostat -x 1", "每秒刷新一次磁盘 IO 详情", "Show detailed I/O stats every second")
        e("iostat -dx 2 5", "每 2 秒采样一次，共 5 次", "Sample every 2s, five times")
        tip("await（平均等待毫秒数）持续偏高，说明磁盘响应变慢，需要排查。")
        rel("vmstat", "top", "hdparm", "iotop")
    }

    c("ncdu", "交互式查看目录占用", "A disk usage analyser with an ncurses UI", "ncdu [选项] [路径]", level = Level.ADVANCED) {
        detail("比 du 直观：上下键浏览目录，d 键删除，用来快速清理磁盘。", "More visual than du: browse with the arrow keys and press d to delete.")
        p("-x", "不跨越文件系统边界", "Do not cross filesystem boundaries")
        p("-o 文件", "导出扫描结果", "Export the scan to a file")
        e("ncdu /", "从根目录开始分析占用", "Analyse disk usage from /")
        e("ncdu -x /home", "只分析 /home 所在分区", "Analyse the /home filesystem only")
        rel("du", "df", "rm")
    }
}

/** 压缩与归档 */
val ArchiveGroup = group("archive", "压缩与归档", "Archives & Compression", "archive") {

    c("tar", "打包与解包（最常用）", "Archive files into a tarball", "tar [选项] 归档文件 [文件...]") {
        detail(
            "tar 本身只打包不压缩，配合 -z(gzip) / -j(bzip2) / -J(xz) 才会压缩。记住两个组合就够用：czvf 打包、xzvf 解开。",
            "tar only bundles files; add -z, -j or -J to compress too. Two combos cover most needs: czvf to create, xzvf to extract."
        )
        p("-c", "创建归档", "Create an archive")
        p("-x", "解开归档", "Extract an archive")
        p("-t", "查看归档内容", "List archive contents")
        p("-v", "显示过程", "Verbose output")
        p("-f", "指定归档文件名（必须放在最后）", "Use the given archive file (must come last)")
        p("-z / -j / -J", "使用 gzip / bzip2 / xz 压缩", "Compress with gzip / bzip2 / xz")
        p("-C 目录", "解压到指定目录", "Extract into a directory")
        p("--exclude=", "排除匹配的文件", "Exclude matching files")
        e("tar czvf backup.tar.gz ~/docs", "把 docs 目录打包并用 gzip 压缩", "Create a gzipped archive of docs")
        e("tar xzvf backup.tar.gz", "解包到当前目录", "Extract into the current directory")
        e("tar xzvf backup.tar.gz -C /tmp", "解压到 /tmp", "Extract into /tmp")
        e("tar tzvf backup.tar.gz | head", "不解包先看看里面有什么", "List the contents first")
        e("tar czvf site.tar.gz --exclude='node_modules' ./site", "打包时排除目录", "Archive while excluding node_modules")
        tip("解压前先用 tar t 看一眼，避免文件散落一地（tarbomb）。")
        rel("gzip", "zip", "xz", "rsync", "cpio")
    }

    c("gzip", "压缩文件（.gz）", "Compress files with gzip", "gzip [选项] 文件") {
        detail("压缩后原文件默认被删除；解压用 gunzip 或 gzip -d。", "The original file is removed by default; decompress with gunzip or gzip -d.")
        p("-d", "解压", "Decompress")
        p("-k", "保留原文件", "Keep the original file")
        p("-9", "最高压缩率（更慢）", "Best compression (slower)")
        p("-l", "查看压缩文件信息", "List compressed file contents info")
        e("gzip -k access.log", "压缩日志并保留原文件", "Compress a log and keep the original")
        e("gzip -9 big.sql", "用最高压缩率压缩", "Compress with maximum ratio")
        e("gzip -d file.gz", "解压文件", "Decompress a file")
        rel("gunzip", "zcat", "tar", "xz")
    }

    c("gunzip", "解压 .gz 文件", "Decompress gzip files", "gunzip [选项] 文件.gz") {
        p("-k", "保留压缩包", "Keep the compressed file")
        p("-c", "输出到标准输出，不落盘", "Write to stdout")
        e("gunzip file.gz", "解压并删除压缩包", "Decompress and remove the .gz")
        e("gunzip -c file.gz | less", "不解包直接查看内容", "View the content without extracting")
        rel("gzip", "zcat", "zless")
    }

    c("zcat", "直接查看压缩包内容", "Print compressed file contents to stdout", "zcat 文件.gz") {
        detail("不用先解压就能 grep/less，处理大日志时非常省空间。", "Lets you grep or page a compressed file without extracting it — a huge space saver.")
        e("zcat access.log.gz | grep ' 404 '", "在压缩日志里搜索 404", "Search a gzipped log for 404s")
        e("zcat *.gz | wc -l", "统计所有压缩日志的总行数", "Count lines across all compressed logs")
        rel("zless", "zgrep", "gunzip")
    }

    c("bzip2", "用 bzip2 压缩（.bz2）", "Compress files with bzip2", "bzip2 [选项] 文件") {
        detail("压缩率通常高于 gzip，但速度更慢。", "Usually compresses better than gzip, but slower.")
        p("-d", "解压", "Decompress")
        p("-k", "保留原文件", "Keep the original file")
        e("bzip2 -k data.csv", "压缩并保留原文件", "Compress while keeping the original")
        e("bzip2 -d data.csv.bz2", "解压文件", "Decompress a file")
        rel("bunzip2", "gzip", "xz", "tar")
    }

    c("xz", "用 xz 压缩（压缩率最高）", "Compress files with xz", "xz [选项] 文件") {
        detail("压缩率通常最好，适合长期归档；压缩速度最慢。", "Best compression ratio — ideal for long-term archives, but slowest.")
        p("-d", "解压", "Decompress")
        p("-k", "保留原文件", "Keep the original file")
        p("-T0", "多线程压缩（用满 CPU）", "Use all CPU cores")
        p("-9", "最高压缩率", "Maximum compression")
        e("xz -kT0 big.log", "多线程压缩大日志", "Compress a big log using all cores")
        e("xz -dk big.log.xz", "解压 xz 文件", "Decompress an xz file")
        rel("gzip", "bzip2", "tar")
    }

    c("zip", "创建 .zip 压缩包", "Package and compress files into a zip archive", "zip [选项] 归档.zip 文件...") {
        detail("与 Windows / macOS 互通时首选 zip 格式。", "The format of choice when sharing with Windows or macOS.")
        p("-r", "递归压缩目录", "Recursively add directories")
        p("-e", "设置密码加密", "Encrypt with a password")
        p("-q", "静默模式", "Quiet operation")
        p("-9", "最高压缩率", "Best compression")
        e("zip -r photos.zip ./photos", "把整个目录压成 zip", "Zip a whole directory")
        e("zip -e secret.zip a.txt", "创建带密码的压缩包", "Create an encrypted archive")
        rel("unzip", "tar", "gzip")
    }

    c("unzip", "解压 .zip 文件", "Extract zip archives", "unzip [选项] 归档.zip") {
        p("-l", "查看压缩包内容", "List archive contents")
        p("-d 目录", "解压到指定目录", "Extract into a directory")
        p("-o", "不询问直接覆盖", "Overwrite without prompting")
        p("-O 编码", "指定编码解决中文乱码，如 GBK", "Set the encoding (e.g. GBK) for legacy zips")
        e("unzip -l data.zip", "先看里面有什么", "List the contents first")
        e("unzip data.zip -d ./out", "解压到 out 目录", "Extract into ./out")
        e("unzip -O GBK chinese.zip", "解决 Windows 压缩包中文乱码", "Fix garbled Chinese filenames")
        tip("Windows 打的中文压缩包在 Linux 下乱码，加 -O GBK 通常能解决。")
        rel("zip", "tar", "7z")
    }

    c("7z", "高压缩率的多格式工具", "A file archiver with a high compression ratio", "7z [命令] [选项] 归档文件", level = Level.ADVANCED) {
        detail("支持 7z、zip、tar、rar（解压）等多种格式，压缩率优秀。", "Handles 7z, zip, tar and can extract rar, with excellent compression.")
        p("a", "添加文件到归档", "Add files to an archive")
        p("x", "解压并保留目录结构", "Extract with full paths")
        p("l", "列出归档内容", "List archive contents")
        p("-p密码", "设置密码", "Set a password")
        e("7z a backup.7z ./data", "创建 7z 压缩包", "Create a 7z archive")
        e("7z x backup.7z", "解压 7z 包", "Extract a 7z archive")
        e("7z a -p -mhe=on secret.7z files/", "创建加密且加密文件名的压缩包", "Encrypt contents and file names")
        rel("zip", "tar", "xz", "unrar")
    }

    c("cpio", "另一种归档格式", "Copy files to and from archives", "cpio [选项] < 文件列表或归档", level = Level.ADVANCED) {
        detail("常见于 initramfs 和 RPM 包内部；日常用得比 tar 少。", "Used inside initramfs images and RPM packages; less common than tar day to day.")
        p("-o", "创建归档", "Create an archive")
        p("-i", "解开归档", "Extract an archive")
        p("-t", "列出归档内容", "List contents")
        e("find . -type f | cpio -o > backup.cpio", "把当前目录文件打包成 cpio", "Archive the current directory")
        e("cpio -id < backup.cpio", "解开 cpio 归档", "Extract a cpio archive")
        rel("tar", "find", "rpm")
    }
}
