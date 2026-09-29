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

/** 文件与目录 —— 最先要掌握的 18 个命令 */
val FileDirGroup = group("file", "文件与目录", "Files & Directories", "folder") {

    c("ls", "列出目录里有什么", "List directory contents", "ls [选项] [路径]") {
        detail(
            "ls 是使用频率最高的命令，用来查看某个目录下有哪些文件和子目录。Linux 下以 . 开头的文件是隐藏文件，默认不显示。",
            "ls is the most frequently used command: it shows what is inside a directory. Dot-files (starting with a '.') are hidden by default."
        )
        p("-l", "长格式：显示权限、属主、大小、修改时间", "Long format with permissions, owner, size and mtime")
        p("-a", "显示所有文件，包含隐藏文件", "Show all files, including hidden ones")
        p("-h", "大小以 KB / MB 显示（配合 -l）", "Human-readable sizes (use with -l)")
        p("-t", "按修改时间从新到旧排序", "Sort by modification time, newest first")
        p("-r", "反向排序", "Reverse the sort order")
        p("-R", "递归列出子目录内容", "List subdirectories recursively")
        e("ls -lah", "以长格式列出当前目录的全部文件（含隐藏文件）", "List everything in long, human-readable form")
        e("ls -lt /var/log | head", "查看 /var/log 下最近被修改的文件", "Show recently modified files in /var/log")
        e("ls -l | grep '^-' | wc -l", "统计当前目录下普通文件的个数", "Count regular files in the current directory")
        tip("ls -l 第一列首字母代表类型：- 普通文件，d 目录，l 软链接，b/c 设备文件。")
        rel("cd", "pwd", "find", "tree")
    }

    c("cd", "切换当前工作目录", "Change the current directory", "cd [路径]") {
        detail(
            "cd 是 shell 内建命令，用来在目录树之间移动。不带参数时回到当前用户的家目录。",
            "cd is a shell builtin used to move around the directory tree. With no argument it returns to your home directory."
        )
        p("~", "当前用户的家目录，如 /home/alex", "Your home directory, e.g. /home/alex")
        p("..", "上一级目录", "The parent directory")
        p("-", "回到上一次所在的目录", "Go back to the previous directory")
        e("cd /etc", "进入 /etc 目录", "Enter the /etc directory")
        e("cd ..", "返回上一级目录", "Move up one level")
        e("cd ~/Downloads && ls", "进入下载目录并列出内容", "Enter Downloads and list its content")
        tip("路径分两种：绝对路径以 / 开头（如 /usr/bin），相对路径相对当前目录（如 ./script.sh）。")
        rel("pwd", "ls")
    }

    c("pwd", "显示当前所在目录的绝对路径", "Print the working directory", "pwd") {
        detail(
            "在深层目录里迷路时，pwd 告诉你「我现在在哪」。",
            "When you get lost deep in the tree, pwd tells you where you are."
        )
        e("pwd", "输出当前目录，如 /home/alex/projects", "Print the current directory path")
        e("pwd -P", "显示真实物理路径（跳过软链接）", "Print the physical path, resolving symlinks")
        rel("cd", "ls")
    }

    c("mkdir", "创建新目录", "Create directories", "mkdir [选项] 目录名") {
        detail("创建目录；父目录不存在时需要 -p 递归创建。", "Creates directories; use -p to create missing parents.")
        p("-p", "递归创建多级目录，已存在也不报错", "Create parent directories as needed, no error if existing")
        p("-v", "显示创建过程", "Print a message for each created directory")
        e("mkdir notes", "在当前目录创建 notes 目录", "Create a 'notes' directory")
        e("mkdir -p projects/web/{css,js}", "一次性创建多级目录", "Create a nested directory structure")
        rel("rmdir", "touch", "rm")
    }

    c("rmdir", "删除空目录", "Remove empty directories", "rmdir [选项] 目录名") {
        detail("只能删除空目录；目录里有东西请用 rm -r。", "Only works on empty directories; use rm -r for the rest.")
        p("-p", "连带删除空的父目录", "Remove empty parent directories too")
        e("rmdir tmp", "删除空的 tmp 目录", "Remove the empty tmp directory")
        tip("删不掉通常说明目录非空，用 ls -a 看看有没有隐藏文件。")
        rel("mkdir", "rm")
    }

    c("rm", "删除文件或目录", "Remove files or directories", "rm [选项] 文件或目录") {
        detail(
            "Linux 下删除默认不可恢复，没有回收站。用之前务必确认路径。",
            "Deleting on Linux is permanent — there is no trash bin by default. Always double-check the path."
        )
        p("-r / -R", "递归删除目录及其内容", "Delete directories recursively")
        p("-f", "强制删除，不询问、不报错", "Force removal, never prompt")
        p("-i", "删除前逐个询问确认", "Prompt before every removal")
        p("-v", "显示删除了哪些文件", "Show what is being removed")
        e("rm old.txt", "删除单个文件", "Delete one file")
        e("rm -ri backup/", "交互式递归删除目录", "Recursively remove a directory with confirmation")
        e("rm -f *.log", "强制删除当前目录下所有 .log 文件", "Force-delete all .log files here")
        tip("危险组合 rm -rf / ：它会尝试删除整个系统。永远不要在生产机器上执行。")
        rel("rmdir", "mv", "shred")
    }

    c("cp", "复制文件或目录", "Copy files and directories", "cp [选项] 源 目标") {
        detail("复制文件；复制目录必须加 -r。", "Copies files; add -r to copy directories.")
        p("-r", "递归复制目录", "Copy directories recursively")
        p("-i", "覆盖前询问", "Prompt before overwriting")
        p("-v", "显示复制过程", "Explain what is being copied")
        p("-p", "保留原文件的权限、属主、时间", "Preserve mode, ownership and timestamps")
        p("-u", "只在源文件更新时才复制", "Copy only when the source is newer")
        e("cp a.txt b.txt", "把 a.txt 复制成 b.txt", "Copy a.txt to b.txt")
        e("cp -r ~/docs /mnt/usb/", "把整个 docs 目录拷到 U 盘", "Copy the docs directory to a USB drive")
        e("cp -av src/ dest/", "归档式复制：保留属性并显示过程", "Archive copy with attributes and verbose output")
        rel("mv", "rsync", "scp")
    }

    c("mv", "移动或重命名", "Move or rename files", "mv [选项] 源 目标") {
        detail("同一目录下改名，跨目录则是移动。", "Renames within a directory, moves across directories.")
        p("-i", "覆盖前询问", "Prompt before overwriting")
        p("-n", "绝不覆盖已存在的文件", "Never overwrite an existing file")
        p("-v", "显示移动过程", "Explain what is being moved")
        e("mv draft.txt article.md", "重命名文件", "Rename a file")
        e("mv *.png images/", "把所有 PNG 图片移动到 images 目录", "Move all PNG files into images/")
        tip("批量改名可配合 for 循环：for f in *.jpg; do mv \"\$f\" \"vacation_\$f\"; done")
        rel("cp", "rename")
    }

    c("touch", "创建空文件 / 更新时间戳", "Create empty files or update timestamps", "touch [选项] 文件") {
        detail("文件不存在就新建一个空文件；已存在则把访问和修改时间更新为当前时间。", "Creates an empty file if missing, otherwise updates its timestamps.")
        p("-c", "文件不存在时也不创建", "Do not create the file if it does not exist")
        p("-t", "指定时间戳，格式 [[CC]YY]MMDDhhmm", "Set a specific timestamp")
        e("touch note.md", "新建一个空文件 note.md", "Create an empty note.md")
        e("touch -t 202409011200 old.txt", "把文件时间改成 2024-09-01 12:00", "Set the file time to a given value")
        rel("mkdir", "stat")
    }

    c("ln", "创建链接（快捷方式）", "Create links between files", "ln [选项] 源 链接名") {
        detail(
            "软链接（符号链接）像 Windows 快捷方式，指向路径；硬链接是同一个文件的第二个名字，不能跨文件系统、不能指向目录。",
            "A symbolic link points to a path (like a shortcut); a hard link is a second name for the same inode and cannot cross filesystems or point to directories."
        )
        p("-s", "创建软链接（最常用）", "Create a symbolic link")
        p("-f", "已存在时强制覆盖", "Overwrite an existing link")
        p("-n", "把已存在的链接当作普通文件处理", "Treat an existing link as a normal file")
        e("ln -s /var/www/html web", "为网站目录创建名为 web 的软链接", "Create a symlink named web to the site root")
        e("ln -sf ~/.dotfiles/vimrc ~/.vimrc", "强制更新配置文件软链接", "Force-update a config symlink")
        tip("查看链接指向哪里：readlink -f 链接名")
        rel("ls", "cp")
    }

    c("find", "按条件查找文件", "Search for files in a directory tree", "find [路径] [表达式]") {
        detail(
            "find 实时遍历目录树，支持按名字、类型、大小、时间查找，并可对结果执行命令。",
            "find walks the tree in real time and can filter by name, type, size or time, then act on the results."
        )
        p("-name", "按文件名匹配（区分大小写，支持通配符）", "Match by name (case-sensitive, wildcards allowed)")
        p("-iname", "按文件名匹配（忽略大小写）", "Match by name, case-insensitive")
        p("-type", "按类型：f 文件 / d 目录 / l 链接", "Filter by type: f file, d directory, l link")
        p("-size", "按大小，如 +100M 表示大于 100MB", "Filter by size, e.g. +100M")
        p("-mtime", "按修改天数，+7 表示 7 天前", "Filter by modification time in days")
        p("-exec", "对找到的每个文件执行命令", "Run a command on each match")
        p("-maxdepth", "限制搜索深度", "Limit how deep to descend")
        e("find . -name '*.log'", "查找当前目录下所有 .log 文件", "Find all .log files here")
        e("find /home -type f -size +500M", "找出大于 500MB 的文件", "Find files larger than 500 MB")
        e("find . -name '*.tmp' -mtime +7 -delete", "删除 7 天前的临时文件", "Delete temp files older than 7 days")
        tip("-exec 结尾要写 \\; 或 +，例如 find . -name '*.txt' -exec cat {} \\;")
        rel("locate", "which", "grep")
    }

    c("locate", "秒级全局查找文件名", "Find files by name using a database", "locate [选项] 关键字") {
        detail("基于预先生成的索引数据库，速度极快，但新建的文件需要先 updatedb 才能搜到。", "Uses a prebuilt index, so it is instant — but new files need 'sudo updatedb' first.")
        p("-i", "忽略大小写", "Case-insensitive match")
        p("-c", "只统计数量", "Only print the number of matches")
        p("-e", "只显示仍然存在的文件", "Only print entries that still exist")
        e("locate nginx.conf", "快速定位 nginx 配置文件", "Quickly find nginx.conf")
        e("sudo updatedb", "更新索引数据库", "Rebuild the locate database")
        rel("find", "which")
    }

    c("tree", "以树状图显示目录结构", "List directories as a tree", "tree [选项] [路径]") {
        p("-L N", "只显示 N 层深度", "Limit the display depth")
        p("-d", "只显示目录", "List directories only")
        p("-h", "显示文件大小", "Show file sizes")
        p("-a", "包含隐藏文件", "Include hidden files")
        e("tree -L 2 ~/projects", "显示项目目录两层结构", "Show two levels of your projects")
        e("tree -dh .", "只看目录结构并显示大小", "Show directories with sizes")
        tip("未预装时用 sudo apt install tree 安装。")
        rel("ls", "find")
    }

    c("file", "识别文件类型", "Determine a file's type", "file [选项] 文件") {
        detail("不看扩展名，而是按文件头部的「魔数」判断真实类型。", "Detects the real type from magic bytes, not from the extension.")
        p("-i", "输出 MIME 类型", "Output the MIME type")
        p("-b", "简洁输出，不带文件名", "Brief mode, omit the filename")
        e("file photo.jpg", "查看图片真实格式", "Check the real format of an image")
        e("file -i *", "批量查看当前目录文件的 MIME 类型", "Show MIME types of everything here")
        rel("stat", "ls")
    }

    c("stat", "查看文件详细属性", "Show detailed file status", "stat [选项] 文件") {
        detail("比 ls -l 更详细：包含 inode、权限位、链接数、atime/mtime/ctime 等。", "More detail than ls -l: inode, permission bits, link count and all three timestamps.")
        p("-c", "自定义输出格式，如 %s 大小、%n 文件名", "Custom output format (%s size, %n name)")
        p("-f", "显示文件系统的信息", "Show filesystem status instead")
        e("stat report.pdf", "查看文件的完整属性", "Show full status of a file")
        e("stat -c '%n %s bytes' *", "只列出文件名和字节数", "List only names and sizes")
        tip("三种时间：atime 读取时间、mtime 内容修改时间、ctime 属性变更时间。")
        rel("ls", "touch", "file")
    }

    c("basename", "取出路径中的文件名", "Strip directory from a path", "basename 路径 [后缀]") {
        e("basename /home/alex/a.txt", "输出 a.txt", "Prints a.txt")
        e("basename /home/alex/a.txt .txt", "输出 a（去掉后缀）", "Prints a, without the suffix")
        rel("dirname", "readlink")
    }

    c("dirname", "取出路径中的目录部分", "Strip the filename from a path", "dirname 路径") {
        e("dirname /home/alex/a.txt", "输出 /home/alex", "Prints /home/alex")
        e("cd \"\$(dirname \"\$(readlink -f \"\$0\")\")\"", "脚本里切换到脚本所在目录", "cd into the script's own directory")
        rel("basename", "pwd")
    }

    c("realpath", "把路径解析成绝对路径", "Resolve a path to its canonical form", "realpath [选项] 路径") {
        p("-e", "路径必须真实存在，否则报错", "Require every component to exist")
        p("-s", "只做路径规范化，不解析软链接", "Only canonicalise, do not resolve symlinks")
        p("--relative-to=", "输出相对某个目录的路径", "Print the path relative to a directory")
        e("realpath ./notes/../a.txt", "输出规范的绝对路径", "Print the canonical absolute path")
        e("realpath --relative-to=/home /home/alex/a.txt", "输出 alex/a.txt", "Print alex/a.txt")
        rel("readlink", "pwd")
    }

    c("rename", "批量重命名文件", "Rename multiple files", "rename '表达式' 文件...") {
        detail("用 Perl 表达式批量改名，适合给几十个文件统一加前缀、换后缀。", "Uses a Perl expression to rename many files at once — great for prefixes and suffix swaps.")
        p("-n", "只预览，不真正改名", "Dry run, show what would happen")
        p("-v", "显示每个改名动作", "Print each rename")
        e("rename 's/\\.jpeg\$/\\.jpg/' *.jpeg", "把 .jpeg 后缀统一改成 .jpg", "Turn .jpeg extensions into .jpg")
        e("rename -n 's/^/2024-/' *.png", "先预览给所有 PNG 加 2024- 前缀", "Preview adding a 2024- prefix")
        tip("改名前先加 -n 预览，确认无误再正式执行。")
        rel("mv", "find")
    }

    c("shred", "安全擦除文件内容", "Overwrite a file to hide its contents", "shred [选项] 文件") {
        detail("反复覆写文件内容后再删除，比普通 rm 更难恢复（对 SSD 与日志型文件系统效果有限）。", "Repeatedly overwrites a file before deleting it; less reliable on SSDs and journaling filesystems.")
        p("-u", "覆写后删除文件", "Deallocate and remove the file after overwriting")
        p("-n N", "覆写 N 次（默认 3 次）", "Overwrite N times (default 3)")
        p("-z", "最后用 0 覆写以隐藏擦除痕迹", "Add a final overwrite with zeros")
        e("shred -u secret.txt", "彻底擦除并删除文件", "Overwrite and remove secret.txt")
        rel("rm", "dd")
    }

    c("install", "复制文件并设置权限", "Copy files and set attributes", "install [选项] 源 目标") {
        detail("常用于安装脚本：一步完成复制 + 设置权限（mkdir -p + cp + chmod 的组合）。", "Common in install scripts: copy plus chmod in one step.")
        p("-m", "设置权限，如 755", "Set the permission mode, e.g. 755")
        p("-d", "创建目录（相当于 mkdir -p）", "Create directories")
        p("-o", "设置属主", "Set the owner")
        e("install -m 755 app.sh /usr/local/bin/app", "安装脚本并赋予可执行权限", "Install a script as an executable")
        e("install -d /opt/myapp", "创建 /opt/myapp 目录", "Create the /opt/myapp directory")
        rel("cp", "chmod", "mkdir")
    }
}
