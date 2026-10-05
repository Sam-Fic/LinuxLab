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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.model.Level
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.components.FilledAssistChip
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    index: Int,
    onBack: () -> Unit,
    onCommand: (Int) -> Unit
) {
    val command = Repository.byIndex(index)
    // 复制反馈走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val copy = rememberCopyAction(snackbarHostState)
    val favorites by UserStore.favorites.collectAsState()

    // Expressive 弹性顶栏：展开时大字显示命令名（等宽），滚动收起
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = command?.name ?: stringResource(R.string.title_command_detail),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                subtitle = { command?.let { Text(it.categoryZh) } },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (command != null) {
                        val fav = favorites.contains(command.index)
                        IconButton(onClick = { UserStore.toggleFavorite(command.index) }) {
                            Icon(
                                imageVector = if (fav) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = if (fav) "取消收藏" else "收藏",
                                tint = if (fav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (command.syntax.isNotBlank()) {
                            IconButton(onClick = { copy(command.syntax) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.cd_copy_syntax))
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
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
                Text(stringResource(R.string.empty_command_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                
        ) {
            // 一句话说明
            item {
                Text(
                    text = command.zh,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                )
                Text(
                    text = command.en,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 2.dp)
                )
                if (command.level == Level.BASIC) {
                    Text(
                        text = stringResource(R.string.label_must_learn),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                }
            }

            if (command.detailZh.isNotBlank()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        // 说明内容容器：M3 Expressive largeIncreased 档（20dp）
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Text(
                                text = stringResource(R.string.label_detailed_notes),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = command.detailZh,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = Spacing.sm)
                            )
                            if (command.detailEn.isNotBlank()) {
                                Text(
                                    text = command.detailEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = Spacing.xs)
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
                        modifier = Modifier.padding(horizontal = Spacing.lg),
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
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
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
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                            )
                        }
                        Column(modifier = Modifier.padding(start = Spacing.md)) {
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
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        // 序号徽章 22.dp + 间隔 8.dp → 下方说明/代码块对齐缩进 = 30.dp
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
                            Spacer(Modifier.width(Spacing.sm))
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
                            modifier = Modifier.padding(start = 30.dp, top = Spacing.xs),
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
                            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                        // 提示内容容器：M3 Expressive largeIncreased 档（20dp）
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(modifier = Modifier.padding(Spacing.lg)) {
                            Icon(
                                Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = Spacing.sm)
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
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            chips.forEach { related ->
                                FilledAssistChip(
                                    onClick = { onCommand(related.index) },
                                    label = {
                                        Text(
                                            related.name,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    },
                                    // 形状走主题默认（M3 Expressive small 档，chips 的标准档位）
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

            item { Spacer(Modifier.height(navBarBottomInset())) }
        }
    }
}
