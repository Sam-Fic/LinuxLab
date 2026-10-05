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

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.ui.ThemePrefs
import com.linuxlab.starter.ui.components.CategoryIcon
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.theme.ThemeMode
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.linuxlab.starter.ui.components.connectedToggleShapes
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

/** 外观与关于：M3 原生设置页 —— 列表行 + 开关 + 分段按钮 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    prefs: ThemePrefs,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }
    val licenseText = remember {
        runCatching {
            context.assets.open("THIRD_PARTY_LICENSES.txt").bufferedReader().use { it.readText() }
        }.getOrDefault("许可文本加载失败")
    }
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                title = { Text(stringResource(R.string.title_about)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                
        ) {
            item { SectionTitle("莫奈动态取色 / Material You") }

            item {
                // M3 标准设置行：ListItem + 尾随 Switch（主色容器，内容色取 on 容器角色）
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        leadingContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        supportingContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    leadingContent = {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = prefs.useDynamicColor,
                            onCheckedChange = { prefs.updateDynamicColor(it) },
                            enabled = supportsDynamic
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (supportsDynamic)
                                "当前系统 Android ${Build.VERSION.RELEASE}（API ${Build.VERSION.SDK_INT}），支持动态取色"
                            else
                                "当前系统 Android ${Build.VERSION.RELEASE}，低于 Android 12，将使用内置配色",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    // 两行以上的“高”行默认顶对齐（剩余空间全堆底部）；
                    // 这里显式垂直居中，让上下边距对称
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 尾随 content lambda 只放标题一个文本（新 API 惯例）；
                    // 副标题走 supportingContent 槽位——两段都塞 lambda 会叠进同一个 Box
                    Text(stringResource(R.string.action_dynamic_color))
                }
            }

            item { SectionTitle("主题模式 / Theme") }

            item {
                // M3 标准单选分段按钮：跟随系统 / 浅色 / 深色
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            ButtonGroupDefaults.ConnectedSpaceBetween
                        )
                    ) {
                        val modes = listOf(
                            ThemeMode.SYSTEM to "跟随系统",
                            ThemeMode.LIGHT to "浅色",
                            ThemeMode.DARK to "深色"
                        )
                        modes.forEachIndexed { index, (mode, label) ->
                            FilledTonalToggleButton(
                                checked = prefs.themeMode == mode,
                                onCheckedChange = { prefs.updateThemeMode(mode) },
                                shapes = connectedToggleShapes(index = index, count = modes.size),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton }
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            item { SectionTitle("内容 / Content") }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    // 信息卡：M3 Expressive largeIncreased 档（20dp）
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        InfoLine("命令与语法条目", "${Repository.totalCount} 条")
                        InfoLine("分类", "${Repository.categoryCount} 个")
                        InfoLine(stringResource(R.string.label_must_learn), "${Repository.basicCount} 条")
                        InfoLine("常见问题排查", "${Repository.faqCount} 条")
                        InfoLine("资源导航", "${Repository.linkCount} 个站点")
                        InfoLine("桌面环境教程", "${Repository.desktopCount} 篇")
                        InfoLine("FHS 目录", "${Repository.fhsCount} 个")
                        InfoLine("界面语言", "中英双语")
                        InfoLine("作者 / Author", "拾星*")
                        InfoLine("开源协议", "GNU GPL v3.0")
                        InfoLine("版本", "1.7.1")
                        TextButton(
                            onClick = { UserStore.replayOnboarding() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.sm)
                        ) {
                            Text(stringResource(R.string.action_replay_onboarding))
                        }
                    }
                }
            }

            item { SectionTitle("开源许可 / Licenses") }

            item {
                Card(
                    onClick = { showLicenses = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    // 许可卡：形状走主题默认（M3 Expressive medium 档，Card 的标准档位）
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.padding(start = Spacing.md).weight(1f)) {
                            Text(stringResource(R.string.about_source_code), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "内置 Alpine Linux、PRoot、BusyBox 等第三方组件及其许可 →",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionTitle("全部分类 / Categories") }

            items(Repository.groups.size) { i ->
                val group = Repository.groups[i]
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(
                        index = i,
                        count = Repository.groups.size
                    ),
                    colors = ListItemDefaults.segmentedColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = 1.dp),
                    leadingContent = { CategoryIcon(key = group.icon, modifier = Modifier.size(22.dp)) },
                    supportingContent = {
                        Text(group.en, style = MaterialTheme.typography.bodySmall)
                    },
                    trailingContent = {
                        Text(
                            "${group.commands.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(group.zh, style = MaterialTheme.typography.bodyLarge)
                }
            }

            item {
                // 隐私说明：M3 Expressive largeIncreased 档（20dp）
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(modifier = Modifier.padding(Spacing.lg)) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "本应用为离线学习工具，不联网、不收集任何数据。命令示例仅供学习，涉及 rm -rf、mkfs、dd 等破坏性命令时请先在测试环境练习。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(start = Spacing.sm)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(navBarBottomInset())) }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(stringResource(R.string.title_open_source_licenses)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = licenseText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) { Text(stringResource(R.string.action_close)) }
            }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
