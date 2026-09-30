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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.model.Danger
import com.linuxlab.starter.model.Faq
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.rememberCopyAction

/**
 * 常见问题排查：现象 → 原因 → 处理步骤（命令可一键复制）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(initialQuery: String = "") {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val list: List<Faq> = remember(query, categoryId) {
        val base = if (query.isBlank()) Repository.faqs else Repository.faqSearch(query)
        val cid = categoryId
        if (cid == null) base else base.filter { it.categoryId == cid }
    }

    val copy = rememberCopyAction()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        placeholder = {
                            Text(
                                "搜问题或报错，如 Permission denied、端口、OOM",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "清空",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { }),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "没有匹配的问题",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "换个关键词试试，或清空筛选条件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoryId == null,
                            onClick = { categoryId = null },
                            label = { Text("全部 ${Repository.faqCount}") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    items(Repository.faqCategories) { cat ->
                        val count = Repository.faqs.count { it.categoryId == cat.id }
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = {
                                categoryId = if (categoryId == cat.id) null else cat.id
                            },
                            label = { Text("${cat.zh} $count") },
                            shape = CircleShape,
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                Text(
                    text = "共 ${list.size} 条 · 命令均可一键复制到实战终端执行",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
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
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (faq.symptom.isNotBlank()) {
                // 同心圆角：外卡 20.dp 与内块间距 16.dp，圆角差 20-16=4.dp
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = faq.symptom,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (expanded) {
                if (faq.danger != Danger.NONE) {
                    val (label, color) = when (faq.danger) {
                        Danger.DANGEROUS -> "高危：可能丢失数据，先备份" to MaterialTheme.colorScheme.error
                        Danger.CAREFUL -> "谨慎：先确认影响范围" to MaterialTheme.colorScheme.tertiary
                        else -> "" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
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
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Text(
                    text = "处理办法",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )

                faq.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(22.dp)
                        ) {
                            androidx.compose.foundation.layout.Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.zh,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (step.command.isNotBlank()) {
                                CodeBlock(
                                    code = step.command,
                                    onCopy = onCopy,
                                    // 同心圆角：外卡 20.dp 与代码块间距 16.dp，圆角差 20-16=4.dp
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
