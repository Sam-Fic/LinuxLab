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

import androidx.graphics.shapes.RoundedPolygon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.model.CommandGroup
import com.linuxlab.starter.model.Level
import com.linuxlab.starter.ui.theme.Spacing
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R


/**
 * 内容直通手势条（小白条悬浮在内容上）时的底部呼吸 =
 * 手势条高度 + extra。配合 Scaffold 关闭 contentWindowInsets 使用：
 * 滚动中内容从小白条下面穿过，滚到底时最后一条自动避开。
 */
@Composable
fun navBarBottomInset(extra: Dp = Spacing.xxl): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + extra

/** 分类图标映射 */
fun categoryIcon(key: String): ImageVector = when (key) {
    "folder" -> Icons.Filled.Folder
    "article" -> Icons.Filled.Description
    "search" -> Icons.Filled.Search
    "lock" -> Icons.Filled.Lock
    "memory" -> Icons.Filled.Memory
    "computer" -> Icons.Filled.Computer
    "package" -> Icons.Filled.Inventory2
    "language" -> Icons.Filled.Language
    "storage" -> Icons.Filled.Storage
    "archive" -> Icons.Filled.Archive
    "misc" -> Icons.Filled.MiscellaneousServices
    "code" -> Icons.Filled.Code
    else -> Icons.Filled.Description
}

/** 分类图标底板形状：M3 官方有机形状，12 类各取其一（Expressive 形状语言） */
fun categoryShape(key: String): RoundedPolygon = when (key) {
    "folder" -> MaterialShapes.Cookie6Sided
    "article" -> MaterialShapes.Pill
    "search" -> MaterialShapes.VerySunny
    "lock" -> MaterialShapes.PuffyDiamond
    "memory" -> MaterialShapes.PixelCircle
    "computer" -> MaterialShapes.Slanted
    "package" -> MaterialShapes.Cookie4Sided
    "language" -> MaterialShapes.Flower
    "storage" -> MaterialShapes.Oval
    "archive" -> MaterialShapes.Triangle
    "misc" -> MaterialShapes.SoftBurst
    "code" -> MaterialShapes.Arrow
    else -> MaterialShapes.Bun
}

@Composable
fun CategoryIcon(
    key: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Icon(
        imageVector = categoryIcon(key),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

/**
 * 复制到剪贴板并提示。
 *
 * - 剪贴板走官方 suspend [androidx.compose.ui.platform.Clipboard] API；
 * - 反馈走 Material 3 官方 [SnackbarHostState]（主题接管配色、跟随深浅色、
 *   可挂动作按钮），不再用系统 Toast —— Toast 不参与 Material 主题体系。
 *
 * 宿主页面需在 Scaffold 上声明 `snackbarHost = { SnackbarHost(hostState) }`
 * 并把同一个 hostState 传进来。
 */
@Composable
fun rememberCopyAction(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    // 必须在 @Composable 作用域取好文案：下面的 lambda 跑在 remember 之外，
    // 在那里调 stringResource 会报「@Composable invocations can only happen…」
    val clipLabel = stringResource(R.string.clip_label)
    val copiedTemplate = stringResource(R.string.snack_copied)
    return remember(clipboard, scope, snackbarHostState, clipLabel, copiedTemplate) {
        { text: String ->
            scope.launch {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(clipLabel, text))
                )
                snackbarHostState.showSnackbar(copiedTemplate.format(text))
            }
        }
    }
}

/**
 * 终端风格的代码块，右侧带复制按钮。
 *
 * 项目字体规则（FontFamily 的唯一裁决标准）：
 * - **等宽**：一切「会在终端里原样出现」的内容——命令名、语法、代码、参数旗标、
 *   键帽、路径、URL、权限位（如 755）、终端屏全部；
 * - **系统默认**：一切自然语言（说明、描述、症状、许可证正文）与数量统计数字。
 *   中文文本即使套等宽族也会回退系统字形，只影响其中的拉丁/数字片段，因此
 *   混排文案（如「读 r·4」）跟随其代码语义取等宽。
 */
@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    onCopy: (String) -> Unit = {},
    /** 容器形状：null 时取主题 token（默认参数无法求值 @Composable，故用可空 + 运行时兜底） */
    shape: Shape? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape ?: MaterialTheme.shapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        // 垂直方向不做外部 padding：高度 = max(文字高 + 上下 12dp, 复制按钮 48dp)，
        // Row 垂直居中 → 单行 / 多行命令的文字始终在色块里上下居中
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = code,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.md),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            IconButton(onClick = { onCopy(code) }) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.cd_copy),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 小节标题：M3 列表分组惯例 —— labelLarge + primary，水平对齐 16dp gutter */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        trailing?.invoke()
    }
}

/** 分类卡片（首页网格） */
@Composable
fun CategoryCard(
    group: CommandGroup,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        // M3 Expressive：大号内容卡取 largeIncreased 档（20dp）
        shape = MaterialTheme.shapes.largeIncreased
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // 图标底板：官方有机形状 blob（与首页分区卡同一套形状语言；
            // 有机形状不受同心圆角约束）
            BlobContainer(
                shape = categoryShape(group.icon),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                CategoryIcon(
                    key = group.icon,
                    modifier = Modifier.padding(Spacing.md),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = group.zh,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.md)
            )
            Text(
                text = "${group.en} · ${group.commands.size} 条",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 搜索结果 / 分类列表里的命令条目：M3 Expressive SegmentedListItem。 */
@Composable
fun CommandRow(
    command: Command,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    showCategory: Boolean = true,
    /** 传了就显示右侧的收藏星标：true=已收藏，false=未收藏 */
    favorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(),
        modifier = modifier
            .fillMaxWidth()
            // 官方分段列表项之间留 2dp 视觉间隔（上下各 1dp）
            .padding(vertical = 1.dp),
        supportingContent = {
            Column(
                modifier = Modifier.padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = command.zh,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = command.en,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showCategory && command.categoryZh.isNotEmpty()) {
                    Text(
                        text = command.categoryZh,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        trailingContent = if (favorite != null && onToggleFavorite != null) {
            {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (favorite) "取消收藏" else "收藏",
                        tint = if (favorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        } else {
            null
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = command.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.primary
            )
            if (command.level == Level.ADVANCED) {
                Text(
                    text = " 进阶",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
