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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.model.Danger
import com.linuxlab.starter.model.Faq
import com.linuxlab.starter.ui.components.showKeyboardOnFocus
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.EmptyState
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.components.FilledFilterChip
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset
import com.linuxlab.starter.ui.components.NumberedRow
import androidx.compose.ui.text.style.TextOverflow

/**
 * 常见问题排查：M3 原生「应用栏 + 搜索框」；
 * 下方为分类筛选 + 现象 → 原因 → 处理步骤（命令可一键复制）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(initialQuery: String = "") {
    val textFieldState = rememberTextFieldState(initialText = initialQuery)
    val searchBarState = rememberSearchBarState()
    val query by remember { derivedStateOf { textFieldState.text.toString() } }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val list: List<Faq> = remember(query, categoryId) {
        val base = if (query.isBlank()) Repository.faqs else Repository.faqSearch(query)
        val cid = categoryId
        if (cid == null) base else base.filter { it.categoryId == cid }
    }

    // 复制反馈走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val copy = rememberCopyAction(snackbarHostState)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.showKeyboardOnFocus(),
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        onSearch = { },
                        placeholder = {
                            Text(stringResource(R.string.search_faq_placeholder))
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (textFieldState.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    textFieldState.edit { replace(0, length, "") }
                                }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.action_clear),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            // 官网式空状态：blob 装图标 + 大字标题
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ,
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "没有匹配的问题",
                    subtitle = "换个关键词试试，或清空筛选条件"
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                ,
            contentPadding = PaddingValues(bottom = navBarBottomInset())
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    item {
                        FilledFilterChip(
                            selected = categoryId == null,
                            onClick = { categoryId = null },
                            label = { Text(stringResource(R.string.filter_all_count, Repository.faqCount)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    items(Repository.faqCategories) { cat ->
                        val count = Repository.faqs.count { it.categoryId == cat.id }
                        FilledFilterChip(
                            selected = categoryId == cat.id,
                            onClick = {
                                categoryId = if (categoryId == cat.id) null else cat.id
                            },
                            label = { Text("${cat.zh} $count") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            items(list, key = { it.id }) { faq ->
                FaqCard(
                    faq = faq,
                    onCopy = copy,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )
            }

            item {
                Text(
                    text = "共 ${list.size} 条 · 命令均可一键复制到实战终端执行",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                )
            }
        }
    }
}

@Composable
private fun FaqCard(
    faq: Faq,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        // M3 Expressive：内容卡取 largeIncreased 档（20dp），卡内边距 16dp
        shape = MaterialTheme.shapes.largeIncreased
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = faq.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Repository.faqCategoryZh(faq.categoryId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (faq.symptom.isNotBlank()) {
                if (expanded) {
                    // 展开态：症状完整展示（同心圆角：外卡 20 − 内块间距 16 = 4dp）
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.sm)
                    ) {
                        Text(
                            text = faq.symptom,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                } else {
                    // 折叠态：单行线索，让列表一屏能看更多条目
                    Text(
                        text = faq.symptom,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            if (expanded) {
                if (faq.danger != Danger.NONE) {
                    val (label, color) = when (faq.danger) {
                        Danger.DANGEROUS -> "高危：可能丢失数据，先备份" to MaterialTheme.colorScheme.error
                        Danger.CAREFUL -> "谨慎：先确认影响范围" to MaterialTheme.colorScheme.tertiary
                        Danger.NONE -> "" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Spacing.md)
                    ) {
                        Icon(
                            Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = color
                        )
                    }
                }

                if (faq.cause.isNotBlank()) {
                    Text(
                        text = "原因：${faq.cause}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = Spacing.md)
                    )
                }

                Text(
                    text = "处理办法",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs)
                )

                faq.steps.forEachIndexed { index, step ->
                    NumberedRow(
                        index = index,
                        modifier = Modifier.padding(top = Spacing.sm)
                    ) {
                        Text(
                            text = step.zh,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (step.command.isNotBlank()) {
                            CodeBlock(
                                code = step.command,
                                onCopy = onCopy,
                                // 同心圆角：外卡 20.dp（shapes.largeIncreased）与代码块间距 16.dp，
                                // 圆角差 20-16=4.dp
                                shape = MaterialTheme.shapes.extraSmall,
                                modifier = Modifier.padding(top = Spacing.sm)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
            }
        }
    }
}
