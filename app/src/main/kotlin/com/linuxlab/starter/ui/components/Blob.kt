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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.linuxlab.starter.ui.theme.Spacing

/*
 * m3.material.io 官网同款的有机形状（blob）表达：
 * 形状取自 M3 官方形状库 MaterialShapes（Cookie / Sunny / Clover / Ghostish …），
 * 颜色取主题角色、动效取 MaterialTheme.motionScheme，不引入任何自定义绘制。
 *
 * 说明：MaterialShapes 的多边形都归一化在约 0..1 的空间里，
 * toShape()/MorphShape 在 createOutline 时按目标尺寸缩放并居中。
 */

/** blob 容器：clip 成官方有机形状并填充主题容器色 */
@Composable
fun BlobContainer(
    shape: RoundedPolygon,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // 官方 toShape() 内部自带 remember(this)：同一形状返回同一 Shape 实例，
    // 重组不产生新对象，clip 的轮廓按尺寸缓存
    Box(
        modifier = modifier
            .clip(shape.toShape())
            .background(color),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * 形状形变 blob（官网招牌动效）：index 变化时在 shapes 之间做 Morph，
 * 形变进度由主题 Expressive 动效方案的 fast 空间弹簧驱动（带一次弹性回弹）。
 */
@Composable
fun MorphingBlob(
    shapes: List<RoundedPolygon>,
    index: Int,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    require(shapes.isNotEmpty()) { "MorphingBlob 至少需要一个形状" }
    val motion = MaterialTheme.motionScheme

    var fromIndex by remember { mutableIntStateOf(index.coerceIn(0, shapes.lastIndex)) }
    var toIndex by remember { mutableIntStateOf(fromIndex) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(index) {
        val target = index.coerceIn(0, shapes.lastIndex)
        if (target != toIndex) {
            fromIndex = toIndex
            toIndex = target
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = motion.fastSpatialSpec())
        }
    }

    val morph = remember(fromIndex, toIndex) { Morph(shapes[fromIndex], shapes[toIndex]) }

    // 进度在 graphicsLayer 块里读取：形变期间只更新图层（重新裁剪路径），
    // 不重组任何组件 —— 这是官网形变动效的“零重组”画法
    Box(
        modifier = modifier
            .graphicsLayer {
                shape = MorphShape(morph, progress.value)
                clip = true
            }
            .background(color),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/** 把 Morph 在指定进度的路径缩放到目标尺寸并居中（做法与官方 RoundedPolygon.toShape 一致） */
private class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress = progress)
        path.transform(Matrix().apply { scale(x = size.width, y = size.height) })
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }

    override fun toString(): String = "MorphShape(progress=$progress)"
}

/** 官网式空状态：大号 blob 装图标 + 大字标题 + 说明文案 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    shape: RoundedPolygon = MaterialShapes.Ghostish
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlobContainer(
            shape = shape,
            color = cs.secondaryContainer,
            modifier = Modifier.size(112.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = cs.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xl)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
    }
}
