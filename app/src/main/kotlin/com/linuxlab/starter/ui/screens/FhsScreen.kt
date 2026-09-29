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

package com.linuxlab.starter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.model.FhsKind
import com.linuxlab.starter.model.FhsNode
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.rememberCopyAction

// ---------------------------------------------------------------- 分类的图标 / 配色 / 名字

@Composable
private fun kindIcon(kind: FhsKind): ImageVector = when (kind) {
    FhsKind.SYSTEM -> Icons.Outlined.Terminal
    FhsKind.CONFIG -> Icons.Outlined.Tune
    FhsKind.DATA -> Icons.Outlined.Storage
    FhsKind.USER -> Icons.Outlined.Person
    FhsKind.DEVICE -> Icons.Outlined.Memory
    FhsKind.MISC -> Icons.Outlined.Folder
}

@Composable
private fun kindColor(kind: FhsKind): Color {
    val cs = MaterialTheme.colorScheme
    return when (kind) {
        FhsKind.SYSTEM -> cs.primary
        FhsKind.CONFIG -> cs.secondary
        FhsKind.DATA -> cs.tertiary
        FhsKind.USER -> cs.secondary
        FhsKind.DEVICE -> cs.primary
        FhsKind.MISC -> cs.onSurfaceVariant
    }
}

private fun kindLabel(kind: FhsKind): String = when (kind) {
    FhsKind.SYSTEM -> "系统与程序"
    FhsKind.CONFIG -> "配置"
    FhsKind.DATA -> "会变的数据"
    FhsKind.USER -> "用户"
    FhsKind.DEVICE -> "设备与内核"
    FhsKind.MISC -> "启动 / 挂载"
}

private val LegendKinds = listOf(
    FhsKind.SYSTEM,
    FhsKind.CONFIG,
    FhsKind.DATA,
    FhsKind.USER,
    FhsKind.DEVICE,
    FhsKind.MISC
)

// ---------------------------------------------------------------- 列表数据

/**
 * 一个顶层目录行，以及它下面要显示的子目录。
 *
 * 关键：子目录不再摊平成独立的列表项，而是父行下面的一整块，
 * 这样展开 / 收起可以对整块做高度动画，滚动时也不会逐行闪烁。
 */
private data class TopRow(
    val node: FhsNode,
    /** 需要显示的子目录（搜索时只保留命中的那几个） */
    val children: List<FhsNode>,
    /** 子目录块是否展开 */
    val childrenExpanded: Boolean
)

private fun matches(node: FhsNode, q: String): Boolean =
    node.path.lowercase().contains(q) ||
        node.zh.lowercase().contains(q) ||
        node.detail.lowercase().contains(q) ||
        node.files.any { it.lowercase().contains(q) }

/**
 * 构建要显示的顶层目录。
 * 搜索时：命中子目录就只显示命中的那几个并强制展开；只命中父目录则按正常状态。
 */
private fun buildTopRows(
    nodes: List<FhsNode>,
    query: String,
    expanded: Set<String>
): List<TopRow> {
    val q = query.trim().lowercase()
    return nodes.mapNotNull { node ->
        if (q.isEmpty()) {
            TopRow(
                node = node,
                children = node.children,
                childrenExpanded = node.children.isNotEmpty() && node.path in expanded
            )
        } else {
            val selfHit = matches(node, q)
            val hitKids = node.children.filter { matches(it, q) }
            when {
                hitKids.isNotEmpty() -> TopRow(node, hitKids, true)
                selfHit -> TopRow(node, node.children, false)
                else -> null
            }
        }
    }
}

// ---------------------------------------------------------------- 页面

/**
 * FHS 目录结构图解。
 *
 * 一棵干净的目录树：点箭头展开子目录，点目录名在**该行下面**就地展开它的说明
 * （手风琴式，同时只展开一个），不另开详情卡、也不再有额外的浮动面板。
 * 顶部的图标图例按「用途」给目录分色，搜索框可以直接按路径或关键词过滤。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FhsScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val copy = rememberCopyAction()

    var query by remember { mutableStateOf("") }
    // 展开子目录的节点：默认全部收起，一进来是一张完整的一级目录清单
    val expanded = remember { mutableStateListOf<String>() }
    // 展开详情的节点；再点一次收起
    var detailPath by remember { mutableStateOf<String?>(null) }

    val rows = remember(query, expanded.size) {
        buildTopRows(Repository.fhsTree, query, expanded.toSet())
    }

    // 进页面时整棵树淡入 + 轻微上浮（只播一次，滚动时不再触发，免得逐行闪烁）
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val listAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "listAlpha"
    )
    val listShift by animateFloatAsState(
        targetValue = if (entered) 0f else 24f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "listShift"
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("FHS 目录结构图解") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .graphicsLayer {
                    alpha = listAlpha
                    translationY = listShift
                },
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Linux 没有盘符：硬盘、U 盘都要「挂载」到这棵唯一的目录树上。" +
                            "点目录名看它是干什么的，点箭头展开子目录。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LegendKinds.forEach { kind -> LegendChip(kind = kind) }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("搜索目录或关键词") },
                        placeholder = { Text("如 log、passwd、挂载、/var") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }

            if (rows.isEmpty()) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有匹配的目录，换个词试试",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                    }
                }
            }

            items(rows, key = { it.node.path }) { row ->
                Column {
                    TreeRow(
                        node = row.node,
                        depth = 0,
                        childrenExpanded = row.childrenExpanded,
                        detailShown = detailPath == row.node.path,
                        onRowClick = {
                            detailPath = if (detailPath == row.node.path) null else row.node.path
                        },
                        onToggle = {
                            if (row.node.path in expanded) expanded.remove(row.node.path)
                            else expanded.add(row.node.path)
                        }
                    )
                    // 说明面板：就地在行下面展开
                    AnimatedVisibility(
                        visible = detailPath == row.node.path,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        DetailPanel(node = row.node, depth = 0, onCopy = copy)
                    }
                    // 子目录：整块高度展开 + 从上方滑入
                    AnimatedVisibility(
                        visible = row.childrenExpanded && row.children.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top) +
                            slideInVertically(initialOffsetY = { -it / 6 }),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Column {
                            row.children.forEach { child ->
                                ChildRow(
                                    node = child,
                                    detailShown = detailPath == child.path,
                                    onRowClick = {
                                        detailPath =
                                            if (detailPath == child.path) null else child.path
                                    },
                                    onCopy = copy
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "共 ${Repository.fhsCount} 个目录 · 依据 FHS 3.0 与主流发行版的实际情况整理",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendChip(kind: FhsKind) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = cs.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = kindIcon(kind),
                contentDescription = null,
                tint = kindColor(kind),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = kindLabel(kind),
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

@Composable
private fun TreeRow(
    node: FhsNode,
    depth: Int,
    childrenExpanded: Boolean,
    detailShown: Boolean,
    onRowClick: () -> Unit,
    onToggle: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val hasChildren = node.children.isNotEmpty()

    // 箭头随展开状态旋转、底色与图标色随选中状态渐变，而不是硬切
    val arrowRotation by animateFloatAsState(
        targetValue = if (childrenExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "arrowRotation"
    )
    val detailRotation by animateFloatAsState(
        targetValue = if (detailShown) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "detailRotation"
    )
    val rowColor by animateColorAsState(
        targetValue = if (detailShown) cs.secondaryContainer else Color.Transparent,
        label = "rowColor"
    )
    val rowIconColor by animateColorAsState(
        targetValue = kindColor(node.kind),
        label = "rowIconColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (8 + depth * 16).dp, end = 12.dp, top = 1.dp, bottom = 1.dp),
        shape = RoundedCornerShape(12.dp),
        color = rowColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRowClick)
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 展开子目录
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(if (hasChildren) Modifier.clickable(onClick = onToggle) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (hasChildren) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = if (childrenExpanded) "折叠" else "展开",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                    )
                }
            }
            Icon(
                imageVector = kindIcon(node.kind),
                contentDescription = null,
                tint = rowIconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = node.path,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = node.zh,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = if (detailShown) 0.9f else 0.4f),
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = detailRotation }
            )
        }
    }
}

/** 子目录行（下面可以就地展开自己的说明） */
@Composable
private fun ChildRow(
    node: FhsNode,
    detailShown: Boolean,
    onRowClick: () -> Unit,
    onCopy: (String) -> Unit
) {
    Column {
        TreeRow(
            node = node,
            depth = 1,
            childrenExpanded = false,
            detailShown = detailShown,
            onRowClick = onRowClick,
            onToggle = {}
        )
        AnimatedVisibility(
            visible = detailShown,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            DetailPanel(node = node, depth = 1, onCopy = onCopy)
        }
    }
}

/** 目录说明：就地展开在该行下面 */
@Composable
private fun DetailPanel(
    node: FhsNode,
    depth: Int,
    onCopy: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = (8 + depth * 16 + 26).dp,
                end = 12.dp,
                top = 2.dp,
                bottom = 8.dp
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cs.surfaceContainerHigh
        ) {
            Column(Modifier.padding(14.dp)) {
                // 标题行：图标 + 路径 + 分类
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = kindIcon(node.kind),
                        contentDescription = null,
                        tint = kindColor(node.kind),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = node.path,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surfaceContainerHighest
                    ) {
                        Text(
                            text = kindLabel(node.kind),
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = node.zh,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface,
                    modifier = Modifier.padding(top = 10.dp)
                )
                if (node.detail.isNotBlank()) {
                    Text(
                        text = node.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (node.files.isNotEmpty()) {
                    Text(
                        text = "里面通常有什么",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    node.files.forEach { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = kindColor(node.kind),
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = file,
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (node.commands.isNotEmpty()) {
                    Text(
                        text = "常用命令",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    node.commands.forEach { cmd ->
                        CodeBlock(code = cmd, modifier = Modifier.padding(bottom = 8.dp), onCopy = onCopy)
                    }
                }

                if (node.tips.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = cs.surfaceContainerHighest
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            node.tips.forEachIndexed { index, tip ->
                                Row(Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cs.onSurfaceVariant
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cs.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
