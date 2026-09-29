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

package com.linuxlab.starter.model

/** 面板位置：没有 / 只有顶部 / 只有底部 / 上下都有 */
enum class PanelPos { NONE, TOP, BOTTOM, BOTH }

/** 启动菜单按钮的样式 */
enum class MenuStyle {
    /** 没有常驻菜单按钮（靠 Super 键或概览呼出） */
    NONE,

    /** 面板最左侧一个纯图标按钮 */
    ICON_LEFT,

    /** 面板最左侧一个带文字的按钮：活动 / 应用程序 / 开始 / 菜单 */
    TEXT_LEFT
}

/** Dock 的形态 */
enum class DockStyle {
    NONE,

    /** 底部居中、宽度约占六成（Deepin / macOS 风格） */
    BOTTOM_CENTER,

    /** 底部贯穿式的长 Dock */
    BOTTOM_WIDE,

    /** 屏幕左侧的竖排 Dock */
    LEFT_RAIL
}

/** 窗口标题栏上「最小化 / 最大化 / 关闭」按钮的位置 */
enum class WinButton { NONE, LEFT, RIGHT }

/**
 * 桌面环境的「布局示意」参数。
 * 界面据此用 Compose 现画一张示意图（不打包任何截图，体积零增加、且能跟随莫奈取色）。
 *
 * 每个字段都对应图上一个看得见的形状，九张图因此各不相同：
 * 面板位置 + 菜单样式 + Dock 形态 + 桌面元素（图标 / 小组件 / Dash / 控制中心）+ 主色调。
 */
data class LayoutSpec(
    /** 面板位置 */
    val panel: PanelPos = PanelPos.NONE,
    /** 启动菜单的样式 */
    val menu: MenuStyle = MenuStyle.NONE,
    /** 菜单按钮上的文字，如「活动」「开始」「应用程序」 */
    val menuLabel: String = "",
    /** Dock 形态 */
    val dock: DockStyle = DockStyle.NONE,
    /** 窗口标题栏按钮位置 */
    val winButton: WinButton = WinButton.RIGHT,
    /** 示例窗口数量（平铺式会画多个） */
    val windows: Int = 1,
    /** 是否为平铺式窗口管理器（不画标题栏、窗口等分排列） */
    val tiling: Boolean = false,
    /** 底部状态条（平铺式 WM 的 i3bar / waybar） */
    val statusBar: Boolean = false,
    /** 屏幕左侧竖排的收藏栏 Dash（按 Super 后出现） */
    val dash: Boolean = false,
    /** 桌面左侧的图标（文件管理器、回收站…） */
    val desktopIcons: Boolean = false,
    /** 桌面上的小组件卡片（KDE Plasma 的标志性元素） */
    val widgets: Boolean = false,
    /** 面板上是否显示窗口列表（任务栏按钮） */
    val windowList: Boolean = false,
    /** 菜单右侧一排快速启动小图标 */
    val launcherRow: Boolean = false,
    /** 面板上的工作区切换器 */
    val workspacePager: Boolean = false,
    /** 面板最右端的「显示桌面」竖条 */
    val showDesktop: Boolean = false,
    /** 屏幕右侧滑出的控制中心卡片 */
    val controlCenter: Boolean = false,
    /** 面板厚度（dp）：LXQt 那条面板很薄，GNOME / Plasma 的比较厚 */
    val panelThickness: Int = 15,
    /** 主色调索引 0..8：九张图各用一种 Material 3 颜色，扫一眼就能区分 */
    val tint: Int = 0
)

/**
 * 由布局参数自动生成的「这张图长什么样」短标签，
 * 直接写在示意图下方，避免用户只看到几张差不多的方块图却不知道该看哪里。
 */
fun LayoutSpec.features(): List<String> {
    val out = mutableListOf<String>()
    when (panel) {
        PanelPos.TOP -> out += "只有顶部面板"
        PanelPos.BOTTOM -> out += "只有底部面板"
        PanelPos.BOTH -> out += "上下两条面板"
        PanelPos.NONE -> if (dock == DockStyle.NONE && !statusBar) out += "默认没有常驻面板"
    }
    when (dock) {
        DockStyle.BOTTOM_CENTER -> out += "底部居中 Dock"
        DockStyle.BOTTOM_WIDE -> out += "底部长条 Dock"
        DockStyle.LEFT_RAIL -> out += "左侧竖排 Dock"
        DockStyle.NONE -> Unit
    }
    if (windowList) out += "面板上有窗口列表"
    if (dash) out += "左侧收藏栏 Dash"
    if (desktopIcons) out += "桌面图标"
    if (widgets) out += "桌面小组件"
    if (launcherRow) out += "快速启动图标"
    if (workspacePager) out += "工作区切换器"
    if (statusBar) out += "底部状态条"
    if (tiling) out += "窗口自动平铺"
    if (controlCenter) out += "侧边控制中心"
    if (showDesktop) out += "显示桌面按钮"
    return out.take(4)
}

/** 一条快捷键 */
data class KeyBinding(
    val keys: String,
    val desc: String
)

/** 某个发行版上的安装命令 */
data class InstallCmd(
    val distro: String,
    val cmd: String
)

/** 一个桌面环境（或一类桌面方案）的教程 */
data class DesktopEnv(
    val id: String,
    val name: String,
    /** 一句话定位 */
    val zh: String,
    /** 图形工具包，如 GTK4 / Qt6 */
    val toolkit: String,
    /** 默认窗口管理器，如 Mutter / KWin / Xfwm4 */
    val windowManager: String,
    /** 资源占用档位：低 / 中 / 偏高 */
    val level: String,
    /** 粗略的空闲内存占用 */
    val memory: String,
    /** 适合谁用 */
    val bestFor: String,
    /** 布局示意图参数 */
    val layout: LayoutSpec,
    /** 示意图要点的中文注解，与图上的 ①②③ 对应 */
    val layoutNotes: List<String>,
    /** 常用快捷键 */
    val shortcuts: List<KeyBinding>,
    /** 各发行版安装命令 */
    val installs: List<InstallCmd>,
    /** 新手提示 */
    val tips: List<String>
)
