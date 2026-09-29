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

import com.linuxlab.starter.model.DesktopEnv
import com.linuxlab.starter.model.InstallCmd
import com.linuxlab.starter.model.KeyBinding
import com.linuxlab.starter.model.DockStyle
import com.linuxlab.starter.model.LayoutSpec
import com.linuxlab.starter.model.MenuStyle
import com.linuxlab.starter.model.PanelPos
import com.linuxlab.starter.model.WinButton

/**
 * 桌面环境的分层结构：从内核到应用，桌面环境处在哪一层。
 * 第一项是层名，第二项是这一层在做什么。
 */
val DesktopLayers: List<Pair<String, String>> = listOf(
    "Linux 内核" to "驱动显卡、键盘、磁盘等硬件，上面的一切都跑在它之上",
    "显示服务器 X11 / Wayland" to "负责把窗口画到屏幕上、把鼠标键盘事件分发给应用",
    "显示管理器 GDM / SDDM / LightDM" to "开机后的登录界面，在这里选择「会话」就是选桌面环境",
    "窗口管理器 Mutter / KWin / Xfwm4" to "管窗口的位置、大小、最小化最大化和边框装饰",
    "桌面环境外壳" to "面板、Dock、开始菜单、系统托盘、设置中心、文件管理器",
    "应用程序" to "浏览器、编辑器、终端等你真正用来干活的程序"
)

/** 桌面环境教程（顺序即列表顺序） */
val DesktopEnvs: List<DesktopEnv> = listOf(

    DesktopEnv(
        id = "gnome",
        name = "GNOME",
        zh = "最现代的「工作流」桌面：靠 Super 键进概览，用工作区组织任务，触控板手势友好",
        toolkit = "GTK4",
        windowManager = "Mutter",
        level = "偏高",
        memory = "约 1.0 ~ 1.5 GB",
        bestFor = "笔记本 / 触控屏用户，喜欢极简、愿意用工作区管理窗口的人",
        layout = LayoutSpec(
            panel = PanelPos.TOP,
            menu = MenuStyle.TEXT_LEFT,
            menuLabel = "活动",
            winButton = WinButton.RIGHT,
            dash = true,
            panelThickness = 18,
            tint = 0
        ),
        layoutNotes = listOf(
            "① 顶栏：左侧「活动」、中间时钟、右侧网络 / 音量 / 电量——GNOME 默认只有这一条栏",
            "② 桌面区：只有当前窗口，默认没有任务栏、没有桌面图标，标题栏仅右侧一个关闭按钮",
            "③ 按 Super（Win）进入「概览」：左侧浮出 Dash 收藏栏，左右可切换工作区"
        ),
        shortcuts = listOf(
            KeyBinding("Super", "打开「活动」概览，可直接打字搜索应用"),
            KeyBinding("Alt + Tab", "在已打开的窗口之间切换"),
            KeyBinding("Super + A", "打开应用列表（九宫格）"),
            KeyBinding("Super + PageUp / PageDown", "切换工作区"),
            KeyBinding("Ctrl + Alt + T", "打开终端（Ubuntu 等发行版默认）"),
            KeyBinding("Super + L", "锁屏")
        ),
        installs = listOf(
            InstallCmd("Debian / Ubuntu", "sudo apt install task-gnome-desktop"),
            InstallCmd("Fedora", "sudo dnf install @gnome-desktop"),
            InstallCmd("Arch", "sudo pacman -S gnome gnome-extra"),
            InstallCmd("openSUSE", "sudo zypper install -t pattern gnome"),
            InstallCmd("Alpine", "setup-desktop gnome")
        ),
        tips = listOf(
            "内存小于 2GB 的老机器别选它，改用 XFCE 或 LXQt",
            "想补上「任务栏 / 最小化按钮 / 桌面图标」，装扩展：sudo apt install gnome-shell-extensions，再到 extensions.gnome.org 装 Dash to Dock",
            "默认用 Wayland 会话；若远程桌面、截屏工具异常，在登录界面齿轮里改选「GNOME on Xorg」"
        )
    ),

    DesktopEnv(
        id = "plasma",
        name = "KDE Plasma",
        zh = "可定制性天花板：从面板到特效几乎都能改，操作逻辑最接近 Windows",
        toolkit = "Qt 6",
        windowManager = "KWin",
        level = "中",
        memory = "约 0.9 ~ 1.4 GB",
        bestFor = "从 Windows 转过来、喜欢折腾美化、想要强大自带工具的人",
        layout = LayoutSpec(
            panel = PanelPos.BOTTOM,
            menu = MenuStyle.ICON_LEFT,
            windowList = true,
            widgets = true,
            winButton = WinButton.RIGHT,
            panelThickness = 18,
            tint = 1
        ),
        layoutNotes = listOf(
            "① 底部面板：左侧 Kickoff 菜单、中间任务管理器、右侧系统托盘",
            "② 窗口标题栏按钮在右上；拖到屏幕边缘自动半屏、拖到顶部自动最大化",
            "③ 桌面右下角可以自由摆放小部件（Widgets）——Plasma 最有辨识度的地方，右键几乎能改一切"
        ),
        shortcuts = listOf(
            KeyBinding("Meta（Win）", "打开 Kickoff 应用菜单"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Meta + ← / →", "窗口贴到左 / 右半屏"),
            KeyBinding("Meta + ↑", "窗口最大化"),
            KeyBinding("Ctrl + Esc", "打开系统活动（进程 / 资源监视器）"),
            KeyBinding("Meta + L", "锁屏")
        ),
        installs = listOf(
            InstallCmd("Debian / Ubuntu", "sudo apt install kde-plasma-desktop"),
            InstallCmd("Kubuntu", "sudo apt install kubuntu-desktop"),
            InstallCmd("Fedora", "sudo dnf install @kde-desktop"),
            InstallCmd("Arch", "sudo pacman -S plasma kde-applications"),
            InstallCmd("Alpine", "setup-desktop plasma")
        ),
        tips = listOf(
            "设置项非常多，新手先只改「系统设置 → 外观与风格」，别一上来全调",
            "NVIDIA 独显建议登录时选 X11 会话（Wayland 下部分驱动仍有兼容问题）",
            "面板可以整条拖到屏幕左侧或右侧，像 Windows 那样竖着放"
        )
    ),

    DesktopEnv(
        id = "xfce",
        name = "XFCE",
        zh = "轻量稳定的老将：传统「顶栏 + 底栏」布局，老机器与虚拟机的首选",
        toolkit = "GTK3",
        windowManager = "Xfwm4",
        level = "低",
        memory = "约 0.5 ~ 0.8 GB",
        bestFor = "老旧电脑、低配虚拟机、只求稳定不求特效的人",
        layout = LayoutSpec(
            panel = PanelPos.BOTH,
            menu = MenuStyle.TEXT_LEFT,
            menuLabel = "应用程序",
            desktopIcons = true,
            launcherRow = true,
            workspacePager = true,
            winButton = WinButton.RIGHT,
            panelThickness = 14,
            tint = 2
        ),
        layoutNotes = listOf(
            "① 上下两条面板：顶栏放 Whisker 菜单 + 窗口按钮 + 通知区，底栏放启动器 + 工作区切换器",
            "② 窗口是普通浮动窗口，标题栏在上方、按钮在右上（Xfwm4 负责装饰与贴边）",
            "③ 桌面左侧默认摆着「主目录 / 回收站」等图标；右键面板 →「面板首选项」可自由增删栏与插件"
        ),
        shortcuts = listOf(
            KeyBinding("Alt + F2", "打开「运行命令」对话框"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区"),
            KeyBinding("Ctrl + Alt + D", "显示桌面（最小化所有窗口）"),
            KeyBinding("Super", "打开 Whisker 菜单（多数发行版已默认绑定）")
        ),
        installs = listOf(
            InstallCmd("Debian / Ubuntu", "sudo apt install xfce4 xfce4-goodies"),
            InstallCmd("Xubuntu", "sudo apt install xubuntu-desktop"),
            InstallCmd("Fedora", "sudo dnf install @xfce-desktop"),
            InstallCmd("Arch", "sudo pacman -S xfce4 xfce4-goodies"),
            InstallCmd("Alpine", "setup-desktop xfce")
        ),
        tips = listOf(
            "启动快、占用低，1~2GB 内存的机器也能流畅跑",
            "还想更省资源：设置管理器 → 窗口管理器调整 → 关掉「合成器」",
            "面板插件很实用：CPU 曲线图、网速、天气、剪贴板管理器都能加"
        )
    ),

    DesktopEnv(
        id = "lxqt",
        name = "LXQt",
        zh = "极简模块化：只留最必要的东西，2GB 内存以下设备的救星",
        toolkit = "Qt",
        windowManager = "Openbox（可换 KWin）",
        level = "低",
        memory = "约 0.4 ~ 0.6 GB",
        bestFor = "十年前的老笔记本、树莓派、瘦客户机、只跑一两个程序的场景",
        layout = LayoutSpec(
            panel = PanelPos.BOTTOM,
            menu = MenuStyle.ICON_LEFT,
            launcherRow = true,
            winButton = WinButton.RIGHT,
            panelThickness = 12,
            tint = 6
        ),
        layoutNotes = listOf(
            "① 底部一条薄面板：菜单 + 快速启动图标 + 任务栏 + 系统托盘",
            "② 窗口是普通浮动窗口，默认没有合成动画和特效，省资源也极少出问题",
            "③ 组件全是模块化的：文件管理器 PCManFM-Qt、终端 QTerminal，都能单独换成别的"
        ),
        shortcuts = listOf(
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Ctrl + Alt + T", "打开终端 QTerminal"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区（Openbox）"),
            KeyBinding("Super", "打开菜单（若无效，到「快捷键」里手动绑定）")
        ),
        installs = listOf(
            InstallCmd("Debian / Ubuntu", "sudo apt install lxqt"),
            InstallCmd("Fedora", "sudo dnf install @lxqt-desktop"),
            InstallCmd("Arch", "sudo pacman -S lxqt"),
            InstallCmd("openSUSE", "sudo zypper install -t pattern lxqt"),
            InstallCmd("Alpine", "setup-desktop lxqt")
        ),
        tips = listOf(
            "默认外观朴素，需要自己去「外观设置」里挑主题和图标",
            "想要一点特效又不想要 KDE 那么重：sudo apt install kwin，再把 KWin 设为窗口管理器",
            "它是 LXDE（GTK 版）的 Qt 接班人，新装机器选 LXQt 不要选 LXDE"
        )
    ),

    DesktopEnv(
        id = "mate",
        name = "MATE",
        zh = "GNOME 2 的续作：三段式菜单 + 双栏面板，怀旧且零学习成本",
        toolkit = "GTK3",
        windowManager = "Marco",
        level = "中低",
        memory = "约 0.6 ~ 0.9 GB",
        bestFor = "习惯 Windows 7 / 早期 GNOME 布局、希望「菜单就在那儿」的人",
        layout = LayoutSpec(
            panel = PanelPos.BOTH,
            menu = MenuStyle.TEXT_LEFT,
            menuLabel = "应用程序",
            desktopIcons = true,
            windowList = true,
            winButton = WinButton.RIGHT,
            panelThickness = 14,
            tint = 3
        ),
        layoutNotes = listOf(
            "① 顶栏放菜单、时钟和通知；底栏放窗口列表，一眼能看到开了哪些程序",
            "② 窗口是最传统的浮动模样：标题栏在上方、按钮在右上",
            "③ 桌面左侧可放图标；菜单是「应用程序 / 位置 / 系统」三段式，右键面板 →「添加到面板」能加几十种小程序"
        ),
        shortcuts = listOf(
            KeyBinding("Alt + F1", "打开应用程序菜单"),
            KeyBinding("Alt + F2", "打开「运行命令」"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区"),
            KeyBinding("Ctrl + Alt + D", "显示桌面"),
            KeyBinding("Super + L", "锁屏")
        ),
        installs = listOf(
            InstallCmd("Ubuntu MATE", "sudo apt install ubuntu-mate-desktop"),
            InstallCmd("Debian", "sudo apt install task-mate-desktop"),
            InstallCmd("Fedora", "sudo dnf install @mate-desktop"),
            InstallCmd("Arch", "sudo pacman -S mate mate-extra"),
            InstallCmd("Alpine", "setup-desktop mate")
        ),
        tips = listOf(
            "想在老机器上找回「熟悉的感觉」，MATE 比 Cinnamon 更省资源",
            "自带的 Caja 文件管理器支持脚本扩展，右键菜单可扩展自己的命令",
            "菜单样式可以换：Brisk Menu 或 Advanced MATE Menu 都更现代"
        )
    ),

    DesktopEnv(
        id = "cinnamon",
        name = "Cinnamon",
        zh = "Linux Mint 的默认桌面：传统布局 + 现代观感，稳定压倒一切",
        toolkit = "GTK3",
        windowManager = "Muffin",
        level = "中",
        memory = "约 0.8 ~ 1.1 GB",
        bestFor = "办公主力机、不想折腾、希望装好就能用的人",
        layout = LayoutSpec(
            panel = PanelPos.BOTTOM,
            menu = MenuStyle.TEXT_LEFT,
            menuLabel = "菜单",
            windowList = true,
            workspacePager = true,
            showDesktop = true,
            winButton = WinButton.RIGHT,
            panelThickness = 16,
            tint = 5
        ),
        layoutNotes = listOf(
            "① 底部面板：左侧菜单 + 中间窗口列表 + 右侧托盘 + 最右「显示桌面」",
            "② 窗口贴边半屏、动画过渡都很顺滑，由 Muffin 窗口管理器处理",
            "③ 右侧是工作区切换器；扩展叫 Spices：applet（面板小程序）、desklet（桌面挂件）、extension（扩展）"
        ),
        shortcuts = listOf(
            KeyBinding("Super", "打开菜单"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Super + D", "显示桌面"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区"),
            KeyBinding("Super + ↑", "窗口最大化"),
            KeyBinding("Super + L", "锁屏")
        ),
        installs = listOf(
            InstallCmd("Linux Mint", "默认已安装，无需额外操作"),
            InstallCmd("Debian / Ubuntu", "sudo apt install cinnamon-desktop-environment"),
            InstallCmd("Fedora", "sudo dnf install @cinnamon-desktop"),
            InstallCmd("Arch", "sudo pacman -S cinnamon"),
            InstallCmd("Alpine", "setup-desktop cinnamon")
        ),
        tips = listOf(
            "更新策略保守，适合当办公主力系统，不会突然换掉你的操作习惯",
            "在「系统设置 → 小程序」里添加天气、网速、剪贴板等面板小程序",
            "Nemo 文件管理器功能很全（内置终端、root 打开、批量重命名）"
        )
    ),

    DesktopEnv(
        id = "deepin",
        name = "Deepin DDE",
        zh = "国产美观派：底部居中 Dock + 右侧滑出控制中心，开箱即用的完成度很高",
        toolkit = "DTK（基于 Qt）",
        windowManager = "deepin-wm / KWin",
        level = "中",
        memory = "约 0.9 ~ 1.3 GB",
        bestFor = "看重颜值与中文体验、希望设置都集中在一个控制中心里的人",
        layout = LayoutSpec(
            panel = PanelPos.NONE,
            dock = DockStyle.BOTTOM_CENTER,
            controlCenter = true,
            winButton = WinButton.RIGHT,
            tint = 4
        ),
        layoutNotes = listOf(
            "① 底部居中的 Dock：启动器 + 常驻应用 + 回收站（默认没有常驻面板）",
            "② 屏幕右侧滑出的是控制中心：网络、蓝牙、显示、账户统统集中在这一处",
            "③ 窗口标题栏按钮在右上；Dock 上右键可切「时尚模式 / 高效模式」，后者更像 Windows 任务栏"
        ),
        shortcuts = listOf(
            KeyBinding("Super", "打开启动器（应用列表）"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区"),
            KeyBinding("Super + D", "显示桌面"),
            KeyBinding("Ctrl + Alt + A", "区域截图")
        ),
        installs = listOf(
            InstallCmd("deepin（官方发行版）", "安装 deepin 系统即自带 DDE，推荐这条"),
            InstallCmd("Arch（AUR）", "yay -S deepin deepin-extra"),
            InstallCmd("Debian / Ubuntu", "需添加 deepin 第三方仓库，稳定性不如官方发行版"),
            InstallCmd("Alpine", "无官方支持")
        ),
        tips = listOf(
            "想完整体验建议直接用 deepin 官方发行版，别在其他系统上拼装",
            "找不到设置？点右下角或按 Super 找「控制中心」，几乎所有设置都在里面",
            "自带应用商店、截图、录屏、看图、音乐等一套国产化应用"
        )
    ),

    DesktopEnv(
        id = "ukui",
        name = "UKUI",
        zh = "麒麟家族的桌面：左下角「开始」菜单 + 任务栏，Windows 用户几乎零成本上手",
        toolkit = "Qt",
        windowManager = "UKWM",
        level = "中低",
        memory = "约 0.7 ~ 1.0 GB",
        bestFor = "国产 CPU 平台、政企办公环境、习惯 Windows 7/10 操作的人",
        layout = LayoutSpec(
            panel = PanelPos.BOTTOM,
            menu = MenuStyle.TEXT_LEFT,
            menuLabel = "开始",
            desktopIcons = true,
            windowList = true,
            workspacePager = true,
            showDesktop = true,
            winButton = WinButton.RIGHT,
            panelThickness = 16,
            tint = 8
        ),
        layoutNotes = listOf(
            "① 底部任务栏：左下角「开始」菜单 + 中间任务区 + 工作区切换器 + 右侧托盘 + 「显示桌面」",
            "② 窗口的样子和 Windows 上一致：标题栏在上方、按钮在右上",
            "③ 桌面左侧默认有「计算机 / 回收站」等图标；另自带控制面板、麒麟助手等国产化组件"
        ),
        shortcuts = listOf(
            KeyBinding("Super", "打开开始菜单"),
            KeyBinding("Alt + Tab", "切换窗口"),
            KeyBinding("Ctrl + Alt + ← / →", "切换工作区"),
            KeyBinding("Super + D", "显示桌面"),
            KeyBinding("Super + L", "锁屏")
        ),
        installs = listOf(
            InstallCmd("优麒麟 / 银河麒麟", "系统自带，无需额外安装"),
            InstallCmd("Ubuntu（Kylin 源）", "sudo apt install ukui-desktop-environment"),
            InstallCmd("Arch（AUR）", "yay -S ukui"),
            InstallCmd("openEuler / openKylin", "在各自软件源中搜索 ukui 组安装")
        ),
        tips = listOf(
            "飞腾、鲲鹏、龙芯等国产 CPU 平台，优先选银河麒麟而不是自己拼桌面",
            "任务栏位置和大小在右键菜单里就能调，不必改配置",
            "界面偏 Windows 风格，若更喜欢 macOS 风格可看 Deepin DDE"
        )
    ),

    DesktopEnv(
        id = "tiling",
        name = "平铺式窗口管理器",
        zh = "i3 / sway / Hyprland：窗口自动排满屏幕、几乎全键盘操作，极客的效率工具",
        toolkit = "无（只有 WM）",
        windowManager = "i3 / sway / Hyprland 自身",
        level = "极低",
        memory = "约 0.1 ~ 0.3 GB（仅 WM）",
        bestFor = "键盘流、多窗口并行工作、愿意花一两天背键位的人",
        layout = LayoutSpec(
            panel = PanelPos.NONE,
            winButton = WinButton.NONE,
            windows = 3,
            tiling = true,
            statusBar = true,
            tint = 7
        ),
        layoutNotes = listOf(
            "① 没有面板：窗口自动等分排列、互不遮挡，连标题栏和边框都省掉了",
            "② 底部状态条（i3bar / waybar）显示当前工作区、CPU、时间，内容与样式自己配",
            "③ 所有行为写在配置文件里：~/.config/i3/config（sway / Hyprland 各有自己的一份）"
        ),
        shortcuts = listOf(
            KeyBinding("Mod + Enter", "打开终端（Mod 通常是 Win 或 Alt）"),
            KeyBinding("Mod + d", "启动程序（dmenu / rofi 模糊搜索）"),
            KeyBinding("Mod + ← / → / ↑ / ↓", "在窗口之间移动焦点"),
            KeyBinding("Mod + Shift + ← / → / ↑ / ↓", "把当前窗口移到该方向"),
            KeyBinding("Mod + 1 ~ 9", "切换到对应工作区"),
            KeyBinding("Mod + Shift + q", "关闭当前窗口"),
            KeyBinding("Mod + e", "平铺 / 浮动布局切换"),
            KeyBinding("Mod + Shift + r", "改完配置后重载，不用重启")
        ),
        installs = listOf(
            InstallCmd("Debian / Ubuntu", "sudo apt install i3 i3status dmenu"),
            InstallCmd("Arch", "sudo pacman -S i3-wm i3status dmenu"),
            InstallCmd("Fedora", "sudo dnf install i3 i3status dmenu"),
            InstallCmd("Wayland 版（sway）", "sudo apt install sway swayidle waybar"),
            InstallCmd("带动画（Hyprland）", "sudo pacman -S hyprland waybar")
        ),
        tips = listOf(
            "学习曲线最陡的一个，建议先在虚拟机或备用机上练熟再上主力机",
            "第一次登录会问是否生成配置文件：选「是」，并记住自己选的 Mod 键是 Win 还是 Alt",
            "它只是窗口管理器，不是完整桌面：网络、蓝牙、电源管理要自己配（或用 waybar + nm-applet 等）"
        )
    )
)
