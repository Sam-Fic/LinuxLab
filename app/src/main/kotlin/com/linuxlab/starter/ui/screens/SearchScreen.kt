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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Faq
import com.linuxlab.starter.ui.components.CommandRow
import com.linuxlab.starter.ui.components.EmptyState
import com.linuxlab.starter.ui.components.FilledFilterChip
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

/** 全局搜索：M3 原生「应用栏 + 搜索框」（AppBarWithSearch），下方为分类筛选与结果列表 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onCommand: (Int) -> Unit,
    onFaq: (String) -> Unit = {}
) {
    val favorites by UserStore.favorites.collectAsState()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val query by remember { derivedStateOf { textFieldState.text.toString() } }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val results: List<com.linuxlab.starter.model.Command> = remember(query, categoryId) {
        val base = if (query.isBlank()) Repository.all else Repository.search(query)
        val cid = categoryId
        if (cid == null) base else base.filter { it.categoryId == cid }
    }

    // 关键词同时命中「常见问题」时，把问题排在最前面
    val faqHits: List<Faq> = remember(query) {
        if (query.isBlank()) emptyList() else Repository.faqSearch(query).take(5)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        onSearch = { },
                        placeholder = {
                            Text(stringResource(R.string.search_command_placeholder))
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
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                
        ) {
            // 分类筛选
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item {
                    FilledFilterChip(
                        selected = categoryId == null,
                        onClick = { categoryId = null },
                        label = { Text(stringResource(R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(Repository.groups) { group ->
                    FilledFilterChip(
                        selected = categoryId == group.id,
                        onClick = {
                            categoryId = if (categoryId == group.id) null else group.id
                        },
                        label = { Text("${group.zh} (${group.commands.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Text(
                text = if (query.isBlank()) "共 ${results.size} 条命令" else "找到 ${results.size} 条结果",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            )

            if (faqHits.isNotEmpty()) {
                Text(
                    text = "常见问题排查",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(bottom = navBarBottomInset(Spacing.sm))
                ) {
                    itemsIndexed(faqHits, key = { _, faq -> faq.id }) { index, faq ->
                        // M3 Expressive 分段列表项：常见问题作为一组，不再用分割线
                        SegmentedListItem(
                            onClick = { onFaq(query) },
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = faqHits.size),
                            colors = ListItemDefaults.segmentedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            supportingContent = {
                                Column {
                                    if (faq.symptom.isNotBlank()) {
                                        Text(
                                            text = faq.symptom,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = Repository.faqCategoryZh(faq.categoryId) + " · 查看处理办法",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = faq.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (results.isEmpty() && faqHits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // 官网式空状态：blob 装图标 + 大字标题
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "没有找到「$query」相关命令",
                        subtitle = "试试更短的关键词，例如 ls、权限、日志、kill"
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    itemsIndexed(results, key = { _, command -> command.index }) { index, command ->
                        CommandRow(
                            command = command,
                            index = index,
                            count = results.size,
                            favorite = favorites.contains(command.index),
                            onToggleFavorite = { UserStore.toggleFavorite(command.index) },
                            onClick = { onCommand(command.index) }
                        )
                    }
                    item { androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp)) }
                }
            }
        }
    }
}
