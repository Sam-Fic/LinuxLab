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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.CommandGroup
import com.linuxlab.starter.ui.components.CategoryIcon
import com.linuxlab.starter.ui.components.CommandRow
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    groupId: String,
    onBack: () -> Unit,
    onCommand: (Int) -> Unit
) {
    val group: CommandGroup? = Repository.groupById(groupId)
    val favorites by UserStore.favorites.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                title = {
                    Column {
                        Text(
                            text = group?.zh ?: stringResource(R.string.title_category_fallback),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = group?.en ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.empty_category_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                ,
            // 底部呼吸与其他列表页一致（24dp = 列表 8 + 页脚 padding 16），最后一条不贴手势条
            contentPadding = PaddingValues(bottom = navBarBottomInset(Spacing.sm))
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    // 分类头卡：M3 Expressive largeIncreased 档（20dp）
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIcon(
                            key = group.icon,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(modifier = Modifier.padding(start = Spacing.lg)) {
                            Text(
                                text = "${group.zh} · ${group.commands.size} 条",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${group.en} — 点击任意命令查看语法、参数与示例",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            itemsIndexed(group.commands, key = { _, command -> command.index }) { index, command ->
                CommandRow(
                    command = command,
                    index = index,
                    count = group.commands.size,
                    showCategory = false,
                    favorite = favorites.contains(command.index),
                    onToggleFavorite = { UserStore.toggleFavorite(command.index) },
                    onClick = { onCommand(command.index) }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.label_mono_font_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.lg)
                )
            }
        }
    }
}
