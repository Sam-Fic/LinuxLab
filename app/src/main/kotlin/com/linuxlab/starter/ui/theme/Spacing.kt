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

package com.linuxlab.starter.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 应用间距分层（4dp 网格）。M3 没有提供间距令牌，这里定义全局唯一档位；
 * 与 [com.linuxlab.starter.ui.theme] 的形状档位（4/8/12/16/20/28）同一节奏。
 *
 * 语义映射：
 * - [xs]  4dp  —— 发丝间距：徽章/键帽内边距、紧贴标题的次行
 * - [sm]  8dp  —— 同组元素间距、卡片间纵向间距、小节标题与内容之间
 * - [md]  12dp —— 密集卡片内边距、网格间距、图块与内容的边距
 * - [lg]  16dp —— 屏幕水平 gutter、常规卡片内边距、小节标题上方
 * - [xl]  20dp —— hero 卡内边距、大区块间隔
 * - [xxl] 24dp —— 列表底部呼吸、页面级留白
 *
 * 规则：结构间距只允许出现这六档；1–2dp 仅限文字基线等发丝级对齐。
 */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
}
