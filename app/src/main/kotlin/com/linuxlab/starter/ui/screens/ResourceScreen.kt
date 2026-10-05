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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.model.LinkItem
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.components.FilledFilterChip
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

/**
 * 资源导航：M3 原生「应用栏 + 搜索框」+ 分类筛选。
 * 点击在系统浏览器中打开，长按复制链接。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceScreen() {
    val context = LocalContext.current
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val query by remember { derivedStateOf { textFieldState.text.toString() } }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val list: List<LinkItem> = remember(query, categoryId) {
        val base = if (query.isBlank()) Repository.links else Repository.linkSearch(query)
        val cid = categoryId
        if (cid == null) base else base.filter { it.categoryId == cid }
    }

    // 复制 / 打开失败都走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
        val ok = runCatching { context.startActivity(intent) }.isSuccess
        if (!ok) {
            scope.launch { snackbarHostState.showSnackbar("没有可用的浏览器") }
        }
    }

    val copy = rememberCopyAction(snackbarHostState)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        onSearch = { },
                        placeholder = {
                            Text(stringResource(R.string.search_link_placeholder))
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
                            label = { Text(stringResource(R.string.filter_all_count, Repository.linkCount)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    items(Repository.linkCategories) { cat ->
                        val count = Repository.links.count { it.categoryId == cat.id }
                        FilledFilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = if (categoryId == cat.id) null else cat.id },
                            label = { Text("${cat.zh} $count") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "点击在浏览器中打开，长按复制链接。内容均为第三方站点，需联网访问。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )
            }

            itemsIndexed(list, key = { _, link -> link.url }) { index, link ->
                LinkRow(
                    link = link,
                    index = index,
                    count = list.size,
                    onClick = { open(link.url) },
                    onLongClick = { copy(link.url) }
                )
            }

            item {
                Text(
                    text = "共 ${list.size} 个站点",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                )
            }
        }
    }
}

/** 资源链接行：M3 Expressive SegmentedListItem，自带 onClick / onLongClick。 */
@Composable
private fun LinkRow(
    link: LinkItem,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        onClick = onClick,
        onLongClick = onLongClick,
        onLongClickLabel = "复制链接",
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier.padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = link.zh,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = link.url.removePrefix("https://").removePrefix("http://").trimEnd('/'),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = link.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "  ${Repository.linkCategoryZh(link.categoryId)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
