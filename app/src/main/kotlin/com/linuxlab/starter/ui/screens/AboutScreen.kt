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
import androidx.compose.foundation.Image
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.data.WallpaperStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.ui.ThemePrefs
import com.linuxlab.starter.ui.components.CategoryIcon
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    prefs: ThemePrefs,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }
    val licenseText = remember {
        runCatching {
            context.assets.open("THIRD_PARTY_LICENSES.txt").bufferedReader().use { it.readText() }
        }.getOrDefault("许可文本加载失败")
    }
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                title = { Text("外观与关于", style = MaterialTheme.typography.titleLarge) }
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                            Text(
                                text = "从壁纸取色",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (supportsDynamic)
                                    "当前系统 Android ${Build.VERSION.RELEASE}（API ${Build.VERSION.SDK_INT}），支持动态取色"
                                else
                                    "当前系统 Android ${Build.VERSION.RELEASE}，低于 Android 12，将使用内置配色",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Switch(
                            checked = prefs.useDynamicColor,
                            onCheckedChange = { prefs.updateDynamicColor(it) },
                            enabled = supportsDynamic
                        )
                    }
                }
            }

            item { SectionTitle("主题模式 / Theme") }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        text = "跟随系统",
                        selected = prefs.themeMode == ThemeMode.SYSTEM,
                        onClick = { prefs.updateThemeMode(ThemeMode.SYSTEM) }
                    )
                    ThemeChip(
                        text = "浅色",
                        selected = prefs.themeMode == ThemeMode.LIGHT,
                        onClick = { prefs.updateThemeMode(ThemeMode.LIGHT) }
                    )
                    ThemeChip(
                        text = "深色",
                        selected = prefs.themeMode == ThemeMode.DARK,
                        onClick = { prefs.updateThemeMode(ThemeMode.DARK) }
                    )
                }
            }

            item { SectionTitle("自定义背景 / Wallpaper") }

            item { WallpaperCard(context = context) }

            item { SectionTitle("内容 / Content") }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        InfoLine("命令与语法条目", "${Repository.totalCount} 条")
                        InfoLine("分类", "${Repository.categoryCount} 个")
                        InfoLine("入门必学", "${Repository.basicCount} 条")
                        InfoLine("常见问题排查", "${Repository.faqCount} 条")
                        InfoLine("资源导航", "${Repository.linkCount} 个站点")
                        InfoLine("桌面环境教程", "${Repository.desktopCount} 篇")
                        InfoLine("FHS 目录", "${Repository.fhsCount} 个")
                        InfoLine("界面语言", "中英双语")
                        InfoLine("作者 / Author", "拾星*")
                        InfoLine("开源协议", "GNU GPL v3.0")
                        InfoLine("版本", "1.7.1")
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { UserStore.replayOnboarding() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("重新观看首次引导 / Replay onboarding")
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text("本应用源码：GNU GPL v3.0", style = MaterialTheme.typography.bodyLarge)
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
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    leadingContent = { CategoryIcon(key = group.icon, modifier = Modifier.size(22.dp)) },
                    headlineContent = { Text(group.zh, style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = {
                        Text(group.en, style = MaterialTheme.typography.bodySmall)
                    },
                    trailingContent = {
                        Text(
                            "${group.commands.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "本应用为离线学习工具，不联网、不收集任何数据。命令示例仅供学习，涉及 rm -rf、mkfs、dd 等破坏性命令时请先在测试环境练习。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text("开源许可 / Licenses") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = licenseText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) { Text("关闭") }
            }
        )
    }
}

@Composable
private fun ThemeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = CircleShape,
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/** 自定义背景：从系统相册选一张图当全应用背景，并可调节浓度 */
@Composable
private fun WallpaperCard(context: android.content.Context) {
    val version by WallpaperStore.version.collectAsState()
    val dim by WallpaperStore.dim.collectAsState()
    val bitmap = remember(version) { WallpaperStore.load() }
    val cs = MaterialTheme.colorScheme

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) WallpaperStore.save(context, uri)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        // 同心圆角：外层 32.dp（shapes.extraLarge）、卡与缩略图间距 16.dp，圆角差 32-16=16.dp
        shape = MaterialTheme.shapes.extraLarge,
        color = cs.surfaceContainerHigh
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cs.surfaceContainerHighest,
                    modifier = Modifier.size(width = 72.dp, height = 54.dp)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "背景图片",
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSurface
                    )
                    Text(
                        text = if (bitmap != null) "已设置 · 全应用生效" else "未设置 · 从相册选一张",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(onClick = { launcher.launch("image/*") }) {
                    Text(if (bitmap != null) "换一张" else "选择图片")
                }
                if (bitmap != null) {
                    TextButton(onClick = { WallpaperStore.clear() }) {
                        Text("移除背景")
                    }
                }
            }

            if (bitmap != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "背景浓度 ${((1f - dim) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.primary
                )
                Slider(
                    value = dim,
                    onValueChange = { WallpaperStore.setDim(it) },
                    valueRange = 0f..0.92f,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "往左拖背景更明显，往右拖文字更清晰",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
