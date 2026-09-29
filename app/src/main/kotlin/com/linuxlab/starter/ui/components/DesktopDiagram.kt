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

package com.linuxlab.starter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linuxlab.starter.model.DockStyle
import com.linuxlab.starter.model.LayoutSpec
import com.linuxlab.starter.model.MenuStyle
import com.linuxlab.starter.model.PanelPos
import com.linuxlab.starter.model.WinButton

/**
 * 桌面环境「布局示意图」。
 *
 * 按 [LayoutSpec] 用 Compose 现画：面板位置、菜单样式、Dock、桌面图标 / 小组件 / Dash /
 * 控制中心、窗口标题栏按钮的位置各不相同，再配一种专属主色，九张图一眼就能区分开。
 *
 * 之所以不塞真实截图：
 *   1. 截图体积大（一张就近 1MB），而这里是纯矢量，APK 大小几乎不变；
 *   2. 示意图只讲「布局与位置」，不会因为版本更新而失真；
 *   3. 配色全部取自 Material 3，能跟随壁纸的莫奈取色。
 *
 * @param detailed 是否画出文字与 ①②③ 编号（列表缩略图传 false，详情页传 true）
 */
@Composable
fun DesktopDiagram(
    spec: LayoutSpec,
    modifier: Modifier = Modifier,
    detailed: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val t = tintOf(cs, spec.tint)
    // 桌面底色：在各桌面统一的浅底上，混一点该桌面环境的主色，远看就能分辨
    val desk = lerp(cs.surfaceContainerHighest, t.panel, 0.18f)
    val marks = marksOf(spec)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier
            .aspectRatio(16f / 10f)
            .clip(shape)
            .background(desk)
            .border(1.dp, cs.outlineVariant, shape)
    ) {
        if (spec.panel == PanelPos.TOP || spec.panel == PanelPos.BOTH) {
            PanelBar(spec, t, isTop = true, mark = marks.topPanel, detailed = detailed)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            DeskBody(spec, t, marks, detailed)
        }
        if (spec.panel == PanelPos.BOTTOM || spec.panel == PanelPos.BOTH) {
            PanelBar(spec, t, isTop = false, mark = marks.bottomPanel, detailed = detailed)
        }
        if (spec.statusBar) StatusBar(t, marks.statusBar, detailed)
    }
}

// ---------------------------------------------------------------- 配色

/** 一张示意图用到的三种颜色：面板底色、面板上的前景色、点缀色（Dock 图标等） */
private class Tint(val panel: Color, val onPanel: Color, val accent: Color)

/** 九张图各用一组 Material 3 颜色，避免「看起来都一样」 */
private fun tintOf(cs: ColorScheme, index: Int): Tint = when (((index % 9) + 9) % 9) {
    // 深
    0 -> Tint(cs.primary, cs.onPrimary, cs.tertiary)
    1 -> Tint(cs.secondary, cs.onSecondary, cs.primary)
    2 -> Tint(cs.tertiary, cs.onTertiary, cs.secondary)
    // 中
    3 -> Tint(cs.primaryFixedDim, cs.onPrimaryFixed, cs.primary)
    4 -> Tint(cs.secondaryFixedDim, cs.onSecondaryFixed, cs.secondary)
    5 -> Tint(cs.tertiaryFixedDim, cs.onTertiaryFixed, cs.tertiary)
    // 浅
    6 -> Tint(cs.primaryContainer, cs.onPrimaryContainer, cs.primary)
    7 -> Tint(cs.secondaryContainer, cs.onSecondaryContainer, cs.secondary)
    else -> Tint(cs.tertiaryContainer, cs.onTertiaryContainer, cs.tertiary)
}

// ---------------------------------------------------------------- ①②③ 编号

private class Marks(
    val topPanel: String,
    val bottomPanel: String,
    val window: String,
    val feature: String,
    val feature2: String,
    val statusBar: String
)

/**
 * 编号分配：有面板时「面板 → 窗口 → 桌面上的特色元素」；
 * 没有面板（Deepin、平铺式）时按屏幕位置从上到下、从显眼到次要。
 * 详情页图注里的 ①②③ 与这里一一对应。
 */
private fun marksOf(spec: LayoutSpec): Marks {
    if (spec.panel != PanelPos.NONE) {
        val isBottomOnly = spec.panel == PanelPos.BOTTOM
        return Marks(
            topPanel = if (isBottomOnly) "" else "1",
            bottomPanel = if (isBottomOnly) "1" else "",
            window = "2",
            feature = "3",
            feature2 = "",
            statusBar = ""
        )
    }
    return when {
        spec.tiling -> Marks("", "", "1", "", "", "2")
        spec.dock != DockStyle.NONE -> Marks("", "", "3", "1", "2", "")
        else -> Marks("", "", "1", "2", "", "")
    }
}

/** 桌面上最醒目的那个特色元素（拿 [Marks.feature]） */
private fun primaryFeature(spec: LayoutSpec): String = when {
    spec.dash -> "dash"
    spec.widgets -> "widgets"
    spec.desktopIcons -> "icons"
    spec.dock != DockStyle.NONE -> "dock"
    spec.controlCenter -> "cc"
    else -> ""
}

/** 第二个特色元素（拿 [Marks.feature2]，目前只有 Deepin 的 Dock + 控制中心） */
private fun secondaryFeature(spec: LayoutSpec): String =
    if (spec.controlCenter && primaryFeature(spec) != "cc") "cc" else ""

@Composable
private fun Marker(text: String, t: Tint, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(t.panel)
            .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = t.onPanel
        )
    }
}

// ---------------------------------------------------------------- 面板

/** 顶栏 / 底栏：菜单按钮、快速启动、窗口列表、工作区、系统托盘 */
@Composable
private fun PanelBar(spec: LayoutSpec, t: Tint, isTop: Boolean, mark: String, detailed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((spec.panelThickness + if (detailed) 3 else 0).dp)
            .background(t.panel),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(5.dp))
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t)
            Spacer(Modifier.width(3.dp))
        }

        // 启动菜单
        when (spec.menu) {
            MenuStyle.TEXT_LEFT -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(t.onPanel.copy(alpha = 0.22f))
                        .then(
                            if (detailed) Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            else Modifier.size(width = 26.dp, height = 9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (detailed) {
                        Text(
                            text = spec.menuLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                            color = t.onPanel,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }

            MenuStyle.ICON_LEFT -> {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(t.onPanel.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(t.panel)
                    )
                }
            }

            MenuStyle.NONE -> Unit
        }

        // 菜单右侧的快速启动图标
        if (spec.launcherRow) {
            Spacer(Modifier.width(4.dp))
            repeat(4) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(t.onPanel.copy(alpha = 0.6f))
                )
                Spacer(Modifier.width(3.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        if (isTop) {
            // 顶栏中间是时钟（GNOME / XFCE / MATE 都是）
            if (detailed) {
                Text(
                    text = "12:30",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                    color = t.onPanel
                )
            } else {
                Box(
                    Modifier
                        .size(width = 12.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(t.onPanel.copy(alpha = 0.55f))
                )
            }
            Spacer(Modifier.weight(1f))
        } else if (spec.windowList) {
            // 底栏中间的窗口列表
            repeat(2) {
                Box(
                    Modifier
                        .size(width = 16.dp, height = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(t.onPanel.copy(alpha = 0.30f))
                )
                Spacer(Modifier.width(3.dp))
            }
        }

        // 工作区切换器
        if (spec.workspacePager) {
            repeat(2) {
                Box(
                    Modifier
                        .size(width = 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(t.onPanel.copy(alpha = 0.45f))
                )
                Spacer(Modifier.width(2.dp))
            }
            Spacer(Modifier.width(3.dp))
        }

        // 系统托盘
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(3) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(t.onPanel.copy(alpha = 0.8f))
                )
                Spacer(Modifier.width(3.dp))
            }
        }

        // 最右端的「显示桌面」竖条
        if (spec.showDesktop) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 10.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(t.onPanel.copy(alpha = 0.7f))
            )
        }
        Spacer(Modifier.width(5.dp))
    }
}

// ---------------------------------------------------------------- 桌面区

@Composable
private fun BoxScope.DeskBody(spec: LayoutSpec, t: Tint, marks: Marks, detailed: Boolean) {
    if (spec.tiling) {
        TilingWindows(spec.windows, t, marks.window, detailed)
        return
    }
    val primary = primaryFeature(spec)
    val secondary = secondaryFeature(spec)

    AppWindow(spec, t, marks.window, detailed)
    if (spec.desktopIcons) {
        DesktopIcons(t, if (primary == "icons") marks.feature else "", detailed)
    }
    if (spec.widgets) {
        Widgets(t, if (primary == "widgets") marks.feature else "", detailed)
    }
    if (spec.dash) {
        DashRail(t, if (primary == "dash") marks.feature else "", detailed)
    }
    if (spec.dock != DockStyle.NONE) {
        Dock(spec, t, if (primary == "dock") marks.feature else "", detailed)
    }
    if (spec.controlCenter) {
        ControlCenter(t, if (secondary == "cc") marks.feature2 else "", detailed)
    }
}

/** 一个普通的浮动窗口 */
@Composable
private fun BoxScope.AppWindow(spec: LayoutSpec, t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    val startPad = if (spec.dash || spec.desktopIcons) 46.dp else 16.dp
    val endPad = if (spec.controlCenter) 58.dp else 16.dp
    val bottomPad = when {
        spec.dock != DockStyle.NONE -> 26.dp
        spec.widgets -> 46.dp
        else -> 12.dp
    }
    val shape = RoundedCornerShape(6.dp)
    Column(
        Modifier
            .padding(start = startPad, end = endPad, top = 12.dp, bottom = bottomPad)
            .fillMaxSize()
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, shape)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (detailed) 12.dp else 8.dp)
                .background(t.panel.copy(alpha = 0.9f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(3.dp))
            if (detailed && mark.isNotEmpty()) Marker(mark, t)
            if (spec.winButton == WinButton.LEFT) WinButtons(t)
            if (detailed) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "终端",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                    color = t.onPanel,
                    maxLines = 1
                )
            }
            Spacer(Modifier.weight(1f))
            if (spec.winButton == WinButton.RIGHT) WinButtons(t)
            Spacer(Modifier.width(3.dp))
        }
        // 窗口内容：几行示意文字
        Column(Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
            repeat(4) { row ->
                Box(
                    Modifier
                        .fillMaxWidth(if (row == 3) 0.45f else 0.8f)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(cs.onSurface.copy(alpha = 0.16f))
                )
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun WinButtons(t: Tint) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) {
            Box(
                Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(t.onPanel.copy(alpha = 0.85f))
            )
            Spacer(Modifier.width(2.dp))
        }
    }
}

/** 桌面左侧的图标（XFCE / MATE / UKUI 默认都有） */
@Composable
private fun BoxScope.DesktopIcons(t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .align(Alignment.TopStart)
            .padding(start = 7.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t)
        }
        repeat(4) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(t.accent.copy(alpha = 0.9f))
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(width = 8.dp, height = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(cs.onSurface.copy(alpha = 0.28f))
                )
            }
        }
    }
}

/** 桌面小组件：右下角一张信息卡片 + 右上角一张小卡片（KDE Plasma） */
@Composable
private fun BoxScope.Widgets(t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 5.dp)) {
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t, Modifier.padding(bottom = 2.dp))
        }
        val card = RoundedCornerShape(6.dp)
        Box(
            Modifier
                .size(width = 66.dp, height = 40.dp)
                .clip(card)
                .background(cs.surface.copy(alpha = 0.94f))
                .border(1.dp, cs.outlineVariant, card)
        ) {
            Column(Modifier.padding(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(t.panel)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier
                            .size(width = 26.dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(cs.onSurface.copy(alpha = 0.25f))
                    )
                }
                Spacer(Modifier.height(5.dp))
                repeat(3) { row ->
                    Box(
                        Modifier
                            .size(width = (44 - row * 12).dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(cs.onSurface.copy(alpha = 0.14f))
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .padding(end = 6.dp, top = 8.dp)
            .size(width = 34.dp, height = 20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(cs.surface.copy(alpha = 0.9f))
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(5.dp))
    )
}

/** 左侧竖排收藏栏 Dash（GNOME 按 Super 后从底部浮出） */
@Composable
private fun BoxScope.DashRail(t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.align(Alignment.CenterStart).padding(start = 5.dp)) {
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t, Modifier.padding(bottom = 3.dp))
        }
        Column(
            Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(cs.surface.copy(alpha = 0.55f))
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(7.dp))
                .padding(horizontal = 2.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(5) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(t.accent.copy(alpha = 0.9f))
                )
            }
        }
    }
}

/** 底部 / 左侧的 Dock */
@Composable
private fun BoxScope.Dock(spec: LayoutSpec, t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    when (spec.dock) {
        DockStyle.BOTTOM_CENTER, DockStyle.BOTTOM_WIDE -> {
            val fraction = if (spec.dock == DockStyle.BOTTOM_CENTER) 0.66f else 0.92f
            val shape = RoundedCornerShape(
                if (spec.dock == DockStyle.BOTTOM_CENTER) 9.dp else 5.dp
            )
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                if (detailed && mark.isNotEmpty()) {
                    Marker(mark, t, Modifier.padding(start = 6.dp, bottom = 2.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(if (spec.dock == DockStyle.BOTTOM_CENTER) 17.dp else 13.dp)
                        .clip(shape)
                        .background(cs.surface.copy(alpha = 0.93f))
                        .border(1.dp, cs.outlineVariant, shape)
                        .padding(horizontal = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(5) {
                        Box(
                            Modifier
                                .size(if (detailed) 10.dp else 9.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(t.accent.copy(alpha = 0.9f))
                        )
                    }
                }
            }
        }

        DockStyle.LEFT_RAIL -> {
            val shape = RoundedCornerShape(7.dp)
            Row(Modifier.align(Alignment.CenterStart).padding(start = 5.dp)) {
                if (detailed && mark.isNotEmpty()) {
                    Marker(mark, t)
                    Spacer(Modifier.width(3.dp))
                }
                Column(
                    Modifier
                        .fillMaxHeight(0.8f)
                        .width(15.dp)
                        .clip(shape)
                        .background(cs.surface.copy(alpha = 0.93f))
                        .border(1.dp, cs.outlineVariant, shape)
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(5) {
                        Box(
                            Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(t.accent.copy(alpha = 0.9f))
                        )
                    }
                }
            }
        }

        DockStyle.NONE -> Unit
    }
}

/** 屏幕右侧滑出的控制中心（Deepin DDE） */
@Composable
private fun BoxScope.ControlCenter(t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(6.dp)
    Column(Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)) {
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t, Modifier.padding(bottom = 3.dp))
        }
        Column(
            Modifier
                .size(width = 46.dp, height = 92.dp)
                .clip(shape)
                .background(cs.surface.copy(alpha = 0.94f))
                .border(1.dp, cs.outlineVariant, shape)
                .padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            repeat(4) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(t.accent.copy(alpha = 0.85f))
                    )
                    Spacer(Modifier.width(5.dp))
                    Box(
                        Modifier
                            .size(width = 14.dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(cs.onSurface.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

/** 平铺式窗口管理器：窗口等分排列、没有标题栏，当前窗口用主色描边 */
@Composable
private fun BoxScope.TilingWindows(count: Int, t: Tint, mark: String, detailed: Boolean) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(count) { index ->
            val active = index == 0
            val shape = RoundedCornerShape(4.dp)
            Box(
                modifier = Modifier
                    .weight(if (active) 1.5f else 1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(if (active) lerp(cs.surface, t.panel, 0.16f) else cs.surface)
                    .border(
                        width = if (active) 1.5.dp else 0.7.dp,
                        color = if (active) t.panel else cs.outlineVariant,
                        shape = shape
                    )
            ) {
                Column(Modifier.padding(start = 6.dp, top = 8.dp)) {
                    if (active && detailed && mark.isNotEmpty()) {
                        Marker(mark, t)
                        Spacer(Modifier.height(4.dp))
                    }
                    repeat(3) { row ->
                        Box(
                            Modifier
                                .fillMaxWidth(if (row == 2) 0.45f else 0.75f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (active) t.panel.copy(alpha = 0.45f)
                                    else cs.onSurface.copy(alpha = 0.16f)
                                )
                        )
                        Spacer(Modifier.height(3.dp))
                    }
                }
            }
        }
    }
}

/** 平铺式 WM 的状态条：左侧工作区标签 + 右侧状态块 */
@Composable
private fun StatusBar(t: Tint, mark: String, detailed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(13.dp)
            .background(t.panel),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(5.dp))
        if (detailed && mark.isNotEmpty()) {
            Marker(mark, t)
            Spacer(Modifier.width(3.dp))
        }
        repeat(5) { index ->
            Box(
                Modifier
                    .size(width = 9.dp, height = 5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index == 0) t.onPanel else t.onPanel.copy(alpha = 0.35f)
                    )
            )
            Spacer(Modifier.width(2.dp))
        }
        Spacer(Modifier.weight(1f))
        repeat(3) {
            Box(
                Modifier
                    .size(width = 11.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(t.onPanel.copy(alpha = 0.45f))
            )
            Spacer(Modifier.width(3.dp))
        }
        Spacer(Modifier.width(5.dp))
    }
}

/**
 * 「桌面环境是怎么叠起来的」分层示意图。
 * 自下而上：内核 → 显示服务器 → 显示管理器 → 窗口管理器 → 桌面外壳 → 应用。
 */
@Composable
fun DesktopLayerDiagram(
    layers: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val tints: List<Color> = listOf(
        cs.surfaceContainerHighest,
        cs.surfaceContainerHigh,
        cs.surfaceContainer,
        cs.secondaryContainer,
        cs.primaryContainer,
        cs.tertiaryContainer
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        layers.forEachIndexed { index, (title, desc) ->
            val bg = tints.getOrNull(index) ?: cs.surfaceContainer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. $title",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
