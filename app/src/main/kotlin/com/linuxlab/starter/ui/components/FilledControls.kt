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

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 色块版 FilterChip。
 *
 * Material 3 默认的 FilterChip 未选中态是描边样式；这里保留官方
 * [FilterChipDefaults.filterChipColors] 的选中配色，同时把未选中态改成
 * surfaceContainerHigh 色块，并通过 `border = null` 去掉 1dp 描边。
 *
 * 官方组件依据：
 * - M3 Chips 的 filled / elevated 视觉族（m3.material.io/components/chips）
 * - Android Compose Chips 文档中的 ElevatedFilterChip 系列
 *   （developer.android.google.cn/develop/ui/compose/components/chip）
 */
@Composable
fun FilledFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SelectableChipColors? = null,
) {
    val cs = MaterialTheme.colorScheme
    val baseColors = colors ?: FilterChipDefaults.filterChipColors()
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = cs.surfaceContainerHigh,
            labelColor = cs.onSurface,
            selectedContainerColor = baseColors.selectedContainerColor,
            selectedLabelColor = baseColors.selectedLabelColor
        )
    )
}

/**
 * 色块版 AssistChip：保留调用方的 container/label 配色，直接去掉默认描边。
 *
 * 官方默认 AssistChip 是 outlined 样式；如果要色块视觉，官方对应的是
 * ElevatedAssistChip，但项目里已有明确的容器色，因此这里用无描边的
 * filled/flat 实现，避免再叠一层 elevation。
 */
@Composable
fun FilledAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ChipColors? = null,
) {
    AssistChip(
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        border = null,
        colors = colors ?: AssistChipDefaults.assistChipColors()
    )
}

/**
 * 官方 connected button group 的首/中/尾形状分配。
 *
 * 参考 AndroidX Material 3 官方 sample：
 * `SingleSelectConnectedButtonGroupSample` / `MultiSelectConnectedButtonGroupSample`
 * 连接组使用 FlowRow/Row + connected shapes + ToggleButton 模式；
 * 这里返回 [ButtonGroupDefaults] 对应的 [ToggleButtonShapes]。
 */
@Composable
fun connectedToggleShapes(index: Int, count: Int): ToggleButtonShapes =
    when {
        index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
        index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
    }
