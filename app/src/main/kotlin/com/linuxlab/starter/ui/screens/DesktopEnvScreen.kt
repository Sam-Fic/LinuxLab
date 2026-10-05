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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.model.DesktopEnv
import com.linuxlab.starter.model.KeyBinding
import com.linuxlab.starter.model.features
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.DesktopDiagram
import com.linuxlab.starter.ui.components.DesktopLayerDiagram
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.components.rememberCopyAction
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

/** 桌面环境教程：列表页 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopEnvScreen(
    onBack: () -> Unit,
    onOpen: (id: String) -> Unit
) {
    // Expressive 弹性顶栏：随内容滚动收起
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.title_desktop_env_tutorial)) },
                subtitle = { Text(stringResource(R.string.subtitle_desktop_env_tutorial)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scrollBehavior
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
                Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                    Text(
                        text = "桌面环境是什么？",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Linux 内核本身只有一个命令行。所谓「桌面环境」是在它之上叠起来的一整套图形界面：" +
                            "面板、开始菜单、Dock、文件管理器、设置中心。下面这张图自下而上标出每一层，" +
                            "登录界面里选「会话」，选的就是其中的桌面环境。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    DesktopLayerDiagram(layers = Repository.desktopLayers)
                }
            }

            item {
                SectionTitle(text = "选一个桌面环境（${Repository.desktopCount} 个）")
            }

            items(Repository.desktops, key = { it.id }) { env ->
                DesktopEnvCard(env = env, onClick = { onOpen(env.id) })
            }
        }
    }
}

@Composable
private fun DesktopEnvCard(env: DesktopEnv, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        // 桌面环境卡：M3 Expressive largeIncreased 档（20dp），内容边距 12dp（密集卡档）
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(Spacing.md)) {
            // 卡头：名称/窗口管理器 + 静态工具包徽章
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = env.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSurface
                    )
                    Text(
                        text = "${env.windowManager} · 资源占用${env.level}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                // 工具包是纯标签：用 Surface 徽章而不是 AssistChip（后者是可交互组件，且 32dp 高视觉过重）
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = cs.secondaryContainer
                ) {
                    Text(
                        text = env.toolkit,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                    )
                }
            }

            // 整幅布局示意图：宽图才看得清面板 / Dock / 桌面元素的差别
            // 同心递进：外层卡 20.dp（largeIncreased）− 内容 12.dp → 图块 8.dp − 边距 4.dp → 示意图 4.dp
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                shape = MaterialTheme.shapes.small,
                color = cs.surfaceContainerLowest
            ) {
                DesktopDiagram(
                    spec = env.layout,
                    detailed = false,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.padding(Spacing.xs)
                )
            }

            // 这张图该看哪里
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                env.layout.features().forEach { feature ->
                    FeatureTag(text = feature)
                }
            }

            Text(
                text = env.zh,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 3,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
    }
}

@Composable
private fun FeatureTag(text: String) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            // 特性标签：走主题 small 档（Expressive 8dp），内边距 8/4（4dp 网格）
            .clip(MaterialTheme.shapes.small)
            .background(cs.surfaceContainerHighest)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/** 桌面环境教程：详情页 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopEnvDetailScreen(id: String, onBack: () -> Unit) {
    val env = Repository.desktopById(id) ?: run {
        Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_desktop_env)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.empty_desktop_env_not_found))
            }
        }
        return
    }
    // 复制反馈走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val copy = rememberCopyAction(snackbarHostState)
    val cs = MaterialTheme.colorScheme

    // Expressive 弹性顶栏：随内容滚动收起
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(env.name) },
                subtitle = { Text("${env.toolkit} · ${env.windowManager}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
                ,
            contentPadding = PaddingValues(bottom = navBarBottomInset())
        ) {
            item {
                Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                    // 大图：与列表卡同一套同心递进 —— 图块 8.dp − 边距 4.dp → 示意图 4.dp
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = cs.surfaceContainerLowest
                    ) {
                        DesktopDiagram(
                            spec = env.layout,
                            detailed = true,
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.padding(Spacing.xs)
                        )
                    }
                    // 图注
                    Column(Modifier.padding(top = Spacing.md)) {
                        env.layoutNotes.forEach { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle(text = "一句话定位")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.primaryContainer
                ) {
                    Text(
                        text = env.zh,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onPrimaryContainer,
                        modifier = Modifier.padding(Spacing.lg)
                    )
                }
            }

            item {
                SectionTitle(text = "基本信息")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        InfoRow("图形工具包", env.toolkit)
                        InfoRow("默认窗口管理器", env.windowManager)
                        InfoRow("资源占用", "${env.level}（${env.memory}）")
                        InfoRow("适合谁", env.bestFor)
                    }
                }
            }

            item { SectionTitle(text = "常用快捷键（${env.shortcuts.size}）") }
            itemsIndexed(env.shortcuts, key = { _, item -> item.keys }) { index, item ->
                KeyRow(
                    binding = item,
                    index = index,
                    count = env.shortcuts.size
                )
            }

            item { SectionTitle(text = "怎么装") }
            item {
                Column(Modifier.padding(horizontal = Spacing.lg)) {
                    env.installs.forEach { install ->
                        Text(
                            text = install.distro,
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.primary,
                            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs)
                        )
                        if (install.cmd.startsWith("sudo") || install.cmd.startsWith("yay") ||
                            install.cmd.startsWith("setup-desktop")
                        ) {
                            CodeBlock(code = install.cmd, onCopy = copy)
                        } else {
                            Text(
                                text = install.cmd,
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionTitle(text = "新手提示") }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.tertiaryContainer
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        env.tips.forEachIndexed { index, tip ->
                            Row(Modifier.padding(vertical = Spacing.xs)) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onTertiaryContainer
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onTertiaryContainer,
                                    modifier = Modifier.padding(start = Spacing.sm)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.lg, top = Spacing.lg, bottom = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Computer,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.padding(end = Spacing.sm)
                    )
                    Text(
                        text = "提示：登录后可在显示管理器（登录界面）的「会话 / 齿轮」里切换已安装的桌面环境。",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/** 快捷键行：M3 Expressive SegmentedListItem，键帽作为 leadingContent。 */
@Composable
private fun KeyRow(
    binding: KeyBinding,
    index: Int,
    count: Int
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(),
        modifier = Modifier
            .fillMaxWidth()
            // 官方分段列表项之间留 2dp 视觉间隔（上下各 1dp）
            .padding(horizontal = Spacing.lg, vertical = 1.dp),
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = binding.keys,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                )
            }
        }
    ) {
        Text(
            text = binding.desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
