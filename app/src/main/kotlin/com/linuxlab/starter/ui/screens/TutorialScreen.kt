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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.StudyPath
import com.linuxlab.starter.data.StudyStep
import com.linuxlab.starter.ui.components.CategoryCard
import com.linuxlab.starter.ui.components.SectionTitle

/**
 * 终端命令教程。
 *
 * 原来首页的「按分类速查」网格整体搬到了这里：先讲命令长什么样、按什么顺序学，
 * 下面才是完整的分类网格（布局与原来一致），点进去就是该分类的命令清单。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onBack: () -> Unit,
    onCategory: (String) -> Unit
) {
    // 「建议的学习顺序」是否展开（旋转屏幕后仍保持用户的选择）；默认折叠，不占版面
    var pathExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("终端命令教程") },
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
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { CommandAnatomyCard() }

            item {
                CollapsibleHeader(
                    title = "建议的学习顺序",
                    subtitle = if (pathExpanded) {
                        "点一下收起"
                    } else {
                        "${StudyPath.size} 步 · 展开看新手该按什么顺序学"
                    },
                    expanded = pathExpanded,
                    onClick = { pathExpanded = !pathExpanded }
                )
            }
            item {
                AnimatedVisibility(
                    visible = pathExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        StudyPath.forEach { step ->
                            StudyStepCard(step = step, onCategory = onCategory)
                        }
                        Text(
                            text = "不确定从哪开始？就按上面的顺序，一步一步往下走。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            item {
                SectionTitle(text = "全部分类 · ${Repository.categoryCount} 类 / ${Repository.totalCount} 条")
            }

            val rows = Repository.groups.chunked(2)
            items(rows.size) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rows[rowIndex].forEach { group ->
                        CategoryCard(
                            group = group,
                            modifier = Modifier.weight(1f),
                            onClick = { onCategory(group.id) }
                        )
                    }
                    if (rows[rowIndex].size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { FooterHint() }
        }
    }
}

/** 命令的三段结构：命令 + 选项 + 参数 */
@Composable
private fun CommandAnatomyCard() {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = cs.primaryContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.padding(7.dp),
                    tint = cs.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "一条命令长什么样",
                style = MaterialTheme.typography.titleMedium,
                color = cs.primary
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = cs.surfaceContainerHigh
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "命令  [选项]  [参数]",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cs.surfaceContainerLowest
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ls",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = cs.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "-lah",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = cs.secondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "/var/log",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = cs.tertiary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                AnatomyLine(color = cs.primary, label = "命令", desc = "要跑哪个程序：ls 负责列出目录内容")
                AnatomyLine(color = cs.secondary, label = "选项", desc = "怎么跑：-l 长格式、-a 含隐藏文件、-h 大小易读")
                AnatomyLine(color = cs.tertiary, label = "参数", desc = "对谁跑：这里是 /var/log，省略则默认当前目录")
            }
        }
    }
}

@Composable
private fun AnatomyLine(color: androidx.compose.ui.graphics.Color, label: String, desc: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(8.dp)
                .padding(0.dp)
        ) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.width(36.dp)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

/** 可折叠的小节标题：点一下展开 / 收起下面的内容 */
@Composable
private fun CollapsibleHeader(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = cs.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = cs.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
            )
        }
    }
}

@Composable
private fun StudyStepCard(step: StudyStep, onCategory: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val index = StudyPath.indexOf(step)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        color = cs.surfaceContainerHigh
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = cs.secondaryContainer,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = step.title.substringAfter("· ").trim(),
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onSurface
                )
            }
            Text(
                text = step.desc,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                step.groupIds.mapNotNull { Repository.groupById(it) }.forEach { group ->
                    AssistChip(
                        onClick = { onCategory(group.id) },
                        label = {
                            Text(
                                "${group.zh} · ${group.commands.size}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = cs.tertiaryContainer,
                            labelColor = cs.onTertiaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterHint() {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = cs.secondaryContainer
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "怎么练",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSecondaryContainer
            )
            Text(
                text = "每条命令都带可一键复制的示例。复制后到底栏「实战终端」里粘贴运行——" +
                    "那是一个真的 Linux 环境（Alpine + proot），随便折腾都不会弄坏手机。",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSecondaryContainer,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
