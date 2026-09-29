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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
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

/** 桌面环境教程：列表页 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopEnvScreen(
    onBack: () -> Unit,
    onOpen: (id: String) -> Unit
) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("桌面环境教程") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(Modifier.height(12.dp))
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = cs.surfaceContainerHigh
    ) {
        Column(Modifier.padding(14.dp)) {
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
                AssistChip(
                    onClick = onClick,
                    label = { Text(env.toolkit, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(8.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cs.secondaryContainer,
                        labelColor = cs.onSecondaryContainer
                    )
                )
            }

            // 整幅布局示意图：宽图才看得清面板 / Dock / 桌面元素的差别
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = cs.surfaceContainerLowest
            ) {
                DesktopDiagram(
                    spec = env.layout,
                    detailed = false,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // 这张图该看哪里
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun FeatureTag(text: String) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cs.surfaceContainerHighest)
            .padding(horizontal = 7.dp, vertical = 3.dp)
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,topBar = {
            TopAppBar(
                title = { Text("桌面环境") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到该桌面环境")
            }
        }
        return
    }
    val copy = rememberCopyAction()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(env.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // 大图
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cs.surfaceContainerLowest
                    ) {
                        DesktopDiagram(
                            spec = env.layout,
                            detailed = true,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    // 图注
                    Column(Modifier.padding(top = 10.dp)) {
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = cs.primaryContainer
                ) {
                    Text(
                        text = env.zh,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            item {
                SectionTitle(text = "基本信息")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = cs.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(14.dp)) {
                        InfoRow("图形工具包", env.toolkit)
                        InfoRow("默认窗口管理器", env.windowManager)
                        InfoRow("资源占用", "${env.level}（${env.memory}）")
                        InfoRow("适合谁", env.bestFor)
                    }
                }
            }

            item { SectionTitle(text = "常用快捷键（${env.shortcuts.size}）") }
            items(env.shortcuts, key = { it.keys }) { KeyRow(it) }

            item { SectionTitle(text = "怎么装") }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    env.installs.forEach { install ->
                        Text(
                            text = install.distro,
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = cs.tertiaryContainer
                ) {
                    Column(Modifier.padding(14.dp)) {
                        env.tips.forEachIndexed { index, tip ->
                            Row(Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onTertiaryContainer
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onTertiaryContainer,
                                    modifier = Modifier.padding(start = 8.dp)
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
                        .padding(start = 20.dp, top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Computer,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
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
            .padding(vertical = 5.dp),
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

@Composable
private fun KeyRow(binding: KeyBinding) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = binding.keys,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = binding.desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )
        }
    }
}
