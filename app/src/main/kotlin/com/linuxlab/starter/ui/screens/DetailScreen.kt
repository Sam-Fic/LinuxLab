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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.model.Level
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.components.rememberCopyAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    index: Int,
    onBack: () -> Unit,
    onCommand: (Int) -> Unit
) {
    val command = Repository.byIndex(index)
    val copy = rememberCopyAction()
    val favorites by UserStore.favorites.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = command?.name ?: "命令详情",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                actions = {
                    if (command != null) {
                        val fav = favorites.contains(command.index)
                        IconButton(onClick = { UserStore.toggleFavorite(command.index) }) {
                            Icon(
                                imageVector = if (fav) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (fav) "取消收藏" else "收藏",
                                tint = if (fav) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                    if (command != null && command.syntax.isNotBlank()) {
                        IconButton(onClick = { copy(command.syntax) }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "复制语法")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (command == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("没有找到该命令", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { HeaderBlock(command) }

            item {
                Text(
                    text = command.zh,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                Text(
                    text = command.en,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }

            if (command.detailZh.isNotBlank()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        // 说明内容容器：形状走主题 token
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "详细说明 / Details",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = command.detailZh,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (command.detailEn.isNotBlank()) {
                                Text(
                                    text = command.detailEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (command.syntax.isNotBlank()) {
                item {
                    SectionTitle("语法 / Syntax")
                    CodeBlock(
                        code = command.syntax,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onCopy = copy
                    )
                }
            }

            if (command.params.isNotEmpty()) {
                item { SectionTitle("常用选项 / Common options") }
                items(command.params) { param ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            // 参数标签徽章：走主题 small 档位
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = param.flag,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = param.zh,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = param.en,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (command.examples.isNotEmpty()) {
                item { SectionTitle("示例 / Examples") }
                itemsIndexed(command.examples) { i, example ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${i + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = example.zh,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (example.en.isNotBlank()) {
                            Text(
                                text = example.en,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 30.dp, top = 2.dp)
                            )
                        }
                        CodeBlock(
                            code = example.code,
                            modifier = Modifier.padding(start = 30.dp, top = 6.dp),
                            onCopy = copy
                        )
                    }
                }
            }

            if (command.tips.isNotEmpty()) {
                item { SectionTitle("小贴士 / Tips") }
                items(command.tips) { tip ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        // 提示内容容器：形状走主题 token
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }

            if (command.related.isNotEmpty()) {
                item { SectionTitle("相关命令 / See also") }
                item {
                    val chips = command.related.mapNotNull { name -> Repository.byName(name) }
                    if (chips.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chips.forEach { related ->
                                AssistChip(
                                    onClick = { onCommand(related.index) },
                                    label = {
                                        Text(
                                            related.name,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    },
                                    shape = CircleShape,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun HeaderBlock(command: Command) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        // 头部大卡：形状走主题 extraLarge 档位
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = command.name,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = command.categoryZh,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (command.level == Level.BASIC) "入门必学" else "进阶",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}
