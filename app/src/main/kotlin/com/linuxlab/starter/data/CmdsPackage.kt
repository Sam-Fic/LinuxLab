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

/** 软件包管理 —— 不同发行版用法对照 */
val PackageGroup = group("package", "软件包管理", "Package Management", "package") {

    c("apt", "Debian / Ubuntu 安装软件", "Install and manage packages (Debian/Ubuntu)", "apt [子命令] [包名]") {
        detail(
            "apt 是 apt-get 的现代替代品，输出更友好。安装软件需要 sudo，因为要写入系统目录。",
            "apt is the modern, friendlier front-end to apt-get. Installing needs sudo because it writes to system directories."
        )
        p("update", "刷新软件源索引（安装前先做）", "Refresh the package index")
        p("upgrade", "升级所有已安装的软件", "Upgrade all installed packages")
        p("install", "安装软件包", "Install packages")
        p("remove", "卸载但保留配置文件", "Remove packages but keep configs")
        p("purge", "彻底卸载并清除配置", "Remove packages and their configs")
        p("search", "搜索软件包", "Search for a package")
        p("show", "查看软件包详情", "Show package details")
        p("autoremove", "删除不再需要的依赖", "Remove unused dependencies")
        e("sudo apt update && sudo apt upgrade -y", "一键更新系统（最常用的第一条命令）", "Update and upgrade everything")
        e("sudo apt install htop", "安装 htop", "Install htop")
        e("apt search 'text editor'", "搜索包含关键字的软件", "Search for a package")
        e("sudo apt purge nginx && sudo apt autoremove", "彻底卸载 nginx 及其无用依赖", "Fully remove nginx and leftovers")
        tip("安装前先 apt update，否则可能装到旧版本或找不到包。")
        rel("apt-get", "dpkg", "snap", "apt-cache")
    }

    c("apt-get", "经典 APT 命令（脚本友好）", "The classic APT command-line tool", "apt-get [子命令] [包名]", level = Level.ADVANCED) {
        detail("输出更稳定、适合写进自动化脚本；日常交互推荐用 apt。", "Its output is stable and script-friendly; use apt for interactive work.")
        p("-y", "自动回答 yes", "Assume yes to prompts")
        p("--no-install-recommends", "只装必需依赖，体积更小", "Skip recommended extras")
        p("-f install", "修复损坏的依赖关系", "Fix broken dependencies")
        e("sudo apt-get install -y --no-install-recommends curl", "最小化安装 curl", "Install curl with minimal dependencies")
        e("sudo apt-get -f install", "修复依赖问题", "Repair broken dependencies")
        e("sudo apt-get clean", "清理下载缓存", "Clean the package cache")
        rel("apt", "dpkg", "apt-cache")
    }

    c("dpkg", "管理 .deb 安装包", "Low-level Debian package manager", "dpkg [选项] 操作", level = Level.ADVANCED) {
        detail("直接安装本地 .deb 文件，或查询某个文件属于哪个包。", "Installs local .deb files and tells you which package owns a file.")
        p("-i", "安装本地 deb 包", "Install a package file")
        p("-r / -P", "卸载 / 彻底清除", "Remove / purge")
        p("-l", "列出已安装的包", "List installed packages")
        p("-L 包名", "列出该包安装了哪些文件", "List files installed by a package")
        p("-S 文件", "查询该文件属于哪个包", "Find the package owning a file")
        e("sudo dpkg -i google-chrome.deb", "安装下载的 deb 包", "Install a downloaded .deb")
        e("dpkg -S /usr/bin/ls", "查看 ls 属于哪个软件包", "Find which package provides /usr/bin/ls")
        e("dpkg -L coreutils | head", "列出 coreutils 安装的文件", "List files from coreutils")
        tip("dpkg -i 后依赖报错就执行 sudo apt -f install 补齐依赖。")
        rel("apt", "rpm", "which")
    }

    c("snap", "跨发行版的沙盒软件包", "Install universal snap packages", "snap [子命令] [包名]") {
        detail("Canonical 推出的容器化包格式，自带依赖、自动更新，体积较大、启动稍慢。", "Canonical's containerised format: bundled dependencies and auto-updates, at the cost of size and startup time.")
        p("find", "搜索 snap 应用", "Search the store")
        p("install", "安装应用", "Install a snap")
        p("list", "列出已安装的应用", "List installed snaps")
        p("refresh", "更新所有 snap", "Update all snaps")
        p("remove", "卸载应用", "Remove a snap")
        e("sudo snap install code --classic", "安装 VS Code（经典模式可访问系统文件）", "Install VS Code in classic mode")
        e("snap list", "查看已安装的 snap", "List installed snaps")
        rel("apt", "flatpak", "dpkg")
    }

    c("flatpak", "桌面应用的通用包格式", "Build, install and run sandboxed desktop apps", "flatpak [子命令] [应用]", level = Level.ADVANCED) {
        detail("常用于 Linux 桌面软件分发，沙盒隔离，跨发行版通用。", "Popular for desktop apps: sandboxed and distribution-independent.")
        p("install", "安装应用", "Install an application")
        p("run", "运行已安装的应用", "Run an installed app")
        p("update", "更新应用", "Update apps")
        p("list", "列出已安装应用", "List installed apps")
        e("flatpak install flathub org.gimp.GIMP", "从 Flathub 安装 GIMP", "Install GIMP from Flathub")
        e("flatpak run org.gimp.GIMP", "启动 GIMP", "Run GIMP")
        rel("snap", "apt", "dnf")
    }

    c("yum", "CentOS 7 / RHEL 7 包管理", "The older RPM package manager", "yum [子命令] [包名]", level = Level.ADVANCED) {
        detail("老版本 Red Hat 系发行版使用；CentOS 8+ 已改用 dnf。", "Used on older Red Hat systems; CentOS 8+ replaced it with dnf.")
        p("install", "安装软件", "Install a package")
        p("remove", "卸载软件", "Remove a package")
        p("update", "更新软件", "Update packages")
        p("provides", "查询某个文件由哪个包提供", "Find which package provides a file")
        e("sudo yum install -y nginx", "安装 nginx", "Install nginx")
        e("yum provides '*/nslookup'", "查找提供 nslookup 的包", "Find the package providing nslookup")
        rel("dnf", "rpm", "apt")
    }

    c("dnf", "Fedora / CentOS 8+ 包管理", "The next-generation RPM package manager", "dnf [子命令] [包名]", level = Level.ADVANCED) {
        detail("yum 的继任者，依赖解析更快，命令基本兼容。", "The successor to yum with faster dependency resolution; the commands are mostly the same.")
        p("install", "安装软件", "Install a package")
        p("remove", "卸载软件", "Remove a package")
        p("search", "搜索软件", "Search for a package")
        p("info", "查看软件包信息", "Show package details")
        p("history", "查看 / 回滚安装历史", "Show or undo transaction history")
        e("sudo dnf install -y git", "安装 git", "Install git")
        e("dnf search 'web server'", "搜索软件", "Search for packages")
        e("sudo dnf history undo last", "回滚上一次安装", "Undo the last transaction")
        rel("yum", "rpm", "pacman")
    }

    c("rpm", "管理 .rpm 安装包", "The RPM package manager", "rpm [选项] 操作", level = Level.ADVANCED) {
        p("-i / -U", "安装 / 升级包", "Install / upgrade a package")
        p("-e", "卸载包", "Erase (remove) a package")
        p("-qa", "列出所有已安装的包", "Query all installed packages")
        p("-ql 包名", "列出包安装的文件", "List files in a package")
        p("-qf 文件", "查询文件属于哪个包", "Find the owning package of a file")
        e("rpm -qa | grep nginx", "检查是否安装了 nginx", "Check whether nginx is installed")
        e("rpm -qf /usr/bin/ls", "查看 ls 属于哪个包", "Find the package that provides ls")
        rel("dnf", "yum", "dpkg")
    }

    c("pacman", "Arch Linux 包管理", "Arch Linux package manager", "pacman [选项] [包名]", level = Level.ADVANCED) {
        detail("Arch / Manjaro 用户的核心命令，-S 同步安装，-Sy 刷新源。", "The core tool on Arch and Manjaro: -S to sync-install, -Sy to refresh.")
        p("-S", "安装软件", "Install packages")
        p("-Syu", "刷新源并全面升级（慎用滚动更新）", "Refresh and upgrade everything")
        p("-Rns", "卸载软件及其无用依赖与配置", "Remove a package with its deps and config")
        p("-Ss", "搜索软件", "Search the repositories")
        p("-Q", "查询已安装的包", "Query the local package database")
        e("sudo pacman -Syu", "滚动更新整个系统", "Perform a full system upgrade")
        e("sudo pacman -S vim", "安装 vim", "Install vim")
        e("pacman -Qe", "列出手动安装的软件", "List explicitly installed packages")
        tip("Arch 滚动更新前建议先看 Arch 官网的新闻公告。")
        rel("dnf", "apt", "yay")
    }

    c("pip", "安装 Python 包", "The Python package installer", "pip [子命令] [包名]") {
        detail("Python 生态的包管理器；系统 Python 之外更推荐用虚拟环境。", "The installer for Python packages; prefer a virtual environment over the system Python.")
        p("install", "安装包", "Install a package")
        p("-r 文件", "按 requirements.txt 批量安装", "Install from a requirements file")
        p("list / freeze", "列出已安装的包 / 导出依赖", "List packages / dump requirements")
        p("uninstall", "卸载包", "Uninstall a package")
        e("pip install requests", "安装 requests 库", "Install the requests library")
        e("pip install -r requirements.txt", "按清单安装项目依赖", "Install project dependencies")
        e("pip freeze > requirements.txt", "导出当前环境依赖", "Export the current environment")
        e("python3 -m venv .venv && source .venv/bin/activate", "创建并进入虚拟环境（推荐做法）", "Create and activate a virtual environment")
        tip("在 Debian/Ubuntu 上，pip 可能触发 externally-managed-environment 错误，用虚拟环境或 --break-system-packages。")
        rel("apt", "npm", "make")
    }

    c("make", "按 Makefile 构建项目", "Build programs with a Makefile", "make [目标]") {
        detail("读取 Makefile 里的规则自动构建，./configure && make && sudo make install 是编译安装的三步曲。", "Runs the rules in a Makefile; the classic trio is ./configure && make && sudo make install.")
        p("-j N", "并行编译，N 通常设为 CPU 核心数", "Parallel build with N jobs")
        p("-n", "只打印要执行的命令，不真正执行", "Dry run")
        p("install", "执行安装目标", "Run the install target")
        p("clean", "清理编译产物", "Remove build artefacts")
        e("make -j\$(nproc)", "用满 CPU 核心并行编译", "Build using all CPU cores")
        e("./configure && make && sudo make install", "经典的源码编译安装流程", "The classic source build sequence")
        rel("gcc", "apt", "cmake")
    }

    c("npm", "安装 Node.js 依赖", "The Node.js package manager", "npm [子命令] [包名]") {
        detail("Node.js 生态的包管理器，配合 package.json 使用。", "The package manager for Node.js, driven by package.json.")
        p("install", "安装依赖（简写 npm i）", "Install dependencies")
        p("install -g", "全局安装命令行工具", "Install a tool globally")
        p("run 脚本", "执行 package.json 中定义的脚本", "Run a script defined in package.json")
        p("update", "更新依赖", "Update dependencies")
        e("npm install", "安装项目全部依赖", "Install all project dependencies")
        e("npm install -g yarn", "全局安装 yarn", "Install yarn globally")
        e("npm run dev", "运行开发服务器", "Start the dev server")
        rel("pip", "make", "apt")
    }
}
