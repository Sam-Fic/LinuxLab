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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.CommandGroup
import com.linuxlab.starter.ui.components.CategoryIcon
import com.linuxlab.starter.ui.components.CommandRow

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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = group?.zh ?: "分类",
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
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("分类不存在", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIcon(
                            key = group.icon,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(modifier = Modifier.padding(start = 14.dp)) {
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

            items(group.commands) { command ->
                CommandRow(
                    command = command,
                    showCategory = false,
                    favorite = favorites.contains(command.index),
                    onToggleFavorite = { UserStore.toggleFavorite(command.index) },
                    onClick = { onCommand(command.index) }
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            item {
                Text(
                    text = "命令名使用等宽字体显示，方便在终端里对照输入。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
