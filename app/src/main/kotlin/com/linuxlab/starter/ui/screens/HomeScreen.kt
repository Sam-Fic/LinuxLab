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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.linuxlab.starter.R
import com.linuxlab.starter.data.HomeSection
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.ui.components.BlobContainer
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.theme.Spacing

// ---------------------------------------------------------------- 首页

/**
 * 首页：品牌区 → 搜索入口 → 今日推荐大卡 → 分区 Carousel 轮播 → 全宽统计带。
 *
 * 板块入口用官方 M3 Expressive 多浏览轮播（HorizontalMultiBrowseCarousel）：
 * 中间是完整大卡、两侧露出相邻卡的一角，横滑切换；顺序固定为枚举序，
 * 不再提供拖动排序（sectionOrder 存储保留以兼容旧数据，首页不再读取）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onDesktops: () -> Unit,
    onTutorial: () -> Unit,
    onChmod: () -> Unit,
    onFhs: () -> Unit,
    onFavorites: () -> Unit,
    onCommand: (Int) -> Unit,
    onSettings: () -> Unit
) {
    val daily = Repository.dailyCommand()
    // 搜索入口的 M3 state（官方 SearchBar 的 state + inputField 重载需要）
    val homeSearchFieldState = rememberTextFieldState()
    val homeSearchBarState = rememberSearchBarState()

    // 搜索入口 = 官方「入口模式」：点击激活的瞬间直接跳转独立搜索页，
    // 不在首页原地展开空面板（SearchBar 的展开态留给真正承载结果的页面）。
    // 跳转前先把入口复位收起，返回首页时仍是收起的官方胶囊。
    LaunchedEffect(homeSearchBarState) {
        snapshotFlow { homeSearchBarState.targetValue }
            .collect { target ->
                if (target == SearchBarValue.Expanded) {
                    onSearch()
                    homeSearchBarState.animateToCollapsed()
                }
            }
    }
    // 复制反馈走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val copyAction = rememberCopyAction(snackbarHostState)
    val favorites by UserStore.favorites.collectAsState()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        // 必须透明：壁纸是画在 AppNav 的最底层背景上的，
        // 用默认的不透明底色会把自定义壁纸整个盖住
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            // 品牌图标装饰块：走主题 small 档位
                            shape = MaterialTheme.shapes.small,
                            color = cs.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = null,
                                modifier = Modifier.padding(Spacing.sm),
                                tint = cs.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text(
                                text = "Linux 入门",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "从第一条命令开始 · Learn Linux step by step",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Palette, contentDescription = stringResource(R.string.cd_appearance_settings))
                    }
                },
                // M3 默认 TopAppBar 就是 surface 容器色；这里显式标注，配合透明 Scaffold 叠在壁纸上（合理例外）
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 搜索入口：M3 标准搜索组件（入口模式，点击即跳搜索页）
            item {
                SearchBar(
                    state = homeSearchBarState,
                    inputField = {
                        SearchBarDefaults.InputField(
                            textFieldState = homeSearchFieldState,
                            searchBarState = homeSearchBarState,
                            onSearch = { onSearch() },
                            placeholder = { Text(stringResource(R.string.search_home_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 今日推荐：整行大卡
            item {
                DailyCard(
                    command = daily,
                    onCopy = copyAction,
                    onClick = { onCommand(daily.index) }
                )
            }

            // 分区入口：官方多浏览轮播
            item {
                SectionCarousel(
                    favCount = favorites.size,
                    onDesktops = onDesktops,
                    onTutorial = onTutorial,
                    onFavorites = onFavorites,
                    onChmod = onChmod,
                    onFhs = onFhs
                )
            }

            // 全宽统计带 + 底部呼吸
            item {
                Column {
                    StatsBand()
                    Spacer(modifier = Modifier.height(96.dp)) // 派生值：顶栏收起位移 + 安全余量
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 分区 Carousel

/** 分区固定顺序（枚举序；轮播不支持拖动重排） */
private val SectionOrder = listOf(
    HomeSection.DESKTOP,
    HomeSection.TUTORIAL,
    HomeSection.FAVORITES,
    HomeSection.CHMOD,
    HomeSection.FHS
)

/** 分区 blob 形状：M3 官方有机形状，与分类卡/引导页同一形状语言 */
private fun sectionShape(id: String): RoundedPolygon = when (id) {
    HomeSection.DESKTOP.id -> MaterialShapes.Slanted
    HomeSection.TUTORIAL.id -> MaterialShapes.Pill
    HomeSection.FAVORITES.id -> MaterialShapes.Heart
    HomeSection.CHMOD.id -> MaterialShapes.PuffyDiamond
    else -> MaterialShapes.ClamShell
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionCarousel(
    favCount: Int,
    onDesktops: () -> Unit,
    onTutorial: () -> Unit,
    onFavorites: () -> Unit,
    onChmod: () -> Unit,
    onFhs: () -> Unit
) {
    val state = rememberCarouselState(itemCount = { SectionOrder.size })
    // 高度自适应：不写死数值，由内容推导 =
    // 图标块 48 + 图标→标题间距 + 标题行高 + 标题→描述间距 + 描述两行行高 + 上下内边距。
    // 行高是 sp→dp，天然跟随系统字体缩放，大字体下轮播自动长高、文字永不裁切。
    val itemHeight = with(LocalDensity.current) {
        val blob = 48.dp
        val titleLine = MaterialTheme.typography.titleLarge.lineHeight.toDp()
        val descLine = MaterialTheme.typography.bodySmall.lineHeight.toDp() * 2
        blob + Spacing.md + titleLine + Spacing.xs + descLine + Spacing.lg * 2
    }
    // 宽度自适应：主卡占可用宽度的 75%（手机上 ≈270dp），平板/分屏/旋转按比例缩放；
    // 限制在 220–400dp，防止极端屏宽下过大或过挤
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val preferredItemWidth = (maxWidth * 0.75f).coerceIn(220.dp, 400.dp)
        HorizontalMultiBrowseCarousel(
            state = state,
            // 多浏览策略：中间完整大卡，两侧露出相邻卡一角
            preferredItemWidth = preferredItemWidth,
            itemSpacing = Spacing.md,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        ) { index ->
            val section = SectionOrder[index]
            val spec = sectionSpec(section.id, favCount)
            val onClick: () -> Unit = when (section) {
                HomeSection.DESKTOP -> onDesktops
                HomeSection.TUTORIAL -> onTutorial
                HomeSection.FAVORITES -> onFavorites
                HomeSection.CHMOD -> onChmod
                else -> onFhs
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // 官方 maskClip：按轮播的遮罩形状裁切（边缘收进时自动变圆角/变窄）。
                    // 首页所有大容器圆角统一走 extraLargeIncreased(32dp) 档
                    .maskClip(MaterialTheme.shapes.extraLargeIncreased)
                    .background(spec.container)
                    .clickable(onClick = onClick)
                    .padding(Spacing.lg)
            ) {
                // 图标底板：官方有机形状（有机形状不受同心圆角约束）
                BlobContainer(
                    shape = sectionShape(section.id),
                    color = spec.content.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = null,
                        tint = spec.content,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = spec.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = spec.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = spec.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = spec.content.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

private data class SectionSpec(
    val icon: ImageVector,
    val title: String,
    val desc: String,
    val container: Color,
    val content: Color
)

@Composable
private fun sectionSpec(id: String, favCount: Int): SectionSpec {
    val cs = MaterialTheme.colorScheme
    return when (id) {
        HomeSection.DESKTOP.id -> SectionSpec(
            icon = Icons.Filled.Computer,
            title = stringResource(R.string.section_desktop_tutorial),
            desc = "9 个桌面 · 图解面板与快捷键",
            container = cs.secondaryContainer,
            content = cs.onSecondaryContainer
        )

        HomeSection.TUTORIAL.id -> SectionSpec(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.section_terminal_tutorial),
            desc = "${Repository.categoryCount} 类 ${Repository.totalCount} 条 · 含学习顺序",
            container = cs.tertiaryContainer,
            content = cs.onTertiaryContainer
        )

        HomeSection.FAVORITES.id -> SectionSpec(
            icon = Icons.Filled.Star,
            title = stringResource(R.string.section_favorites),
            desc = if (favCount == 0) "还没有收藏 · 点星标添加" else "已收藏 $favCount 条命令",
            container = cs.primaryContainer,
            content = cs.onPrimaryContainer
        )

        HomeSection.CHMOD.id -> SectionSpec(
            icon = Icons.Filled.Lock,
            title = stringResource(R.string.section_chmod),
            desc = "勾选 rwx → 755 / 644",
            container = cs.tertiaryContainer,
            content = cs.onTertiaryContainer
        )

        else -> SectionSpec(
            icon = Icons.Filled.FolderOpen,
            title = stringResource(R.string.section_fhs),
            desc = "${Repository.fhsCount} 个目录点开看用途",
            container = cs.surfaceContainerHighest,
            content = cs.onSurface
        )
    }
}

// ---------------------------------------------------------------- 今日推荐大卡

@Composable
private fun DailyCard(
    command: Command,
    onCopy: (String) -> Unit,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        // shapes.extraLargeIncreased = 32dp（Expressive 档位）
        shape = MaterialTheme.shapes.extraLargeIncreased,
        colors = CardDefaults.cardColors(containerColor = cs.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = cs.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(R.string.section_daily_command),
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onPrimaryContainer
                )
            }
            Text(
                text = command.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = cs.onPrimaryContainer,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Text(
                text = command.zh,
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onPrimaryContainer,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            Text(
                text = command.en,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onPrimaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (command.examples.isNotEmpty()) {
                // 同心圆角：外卡 shapes.extraLargeIncreased(32) − 内边距 Spacing.lg(16)
                // = 16dp，正好落在 shapes.large 令牌档
                CodeBlock(
                    code = command.examples.first().code,
                    modifier = Modifier.padding(top = Spacing.lg),
                    onCopy = onCopy,
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 全宽统计带

/** 官网首页式统计带：一条 primaryContainer 大色带 + display 级大数字 */
@Composable
private fun StatsBand() {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        // 首页大容器圆角统一档：extraLargeIncreased(32dp)，与今日推荐卡一致
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = cs.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xl)
        ) {
            StatValue(
                value = "${Repository.totalCount}",
                unit = "条命令",
                modifier = Modifier.weight(1f)
            )
            StatValue(
                value = "${Repository.categoryCount}",
                unit = "个分类",
                modifier = Modifier.weight(1f)
            )
            StatValue(
                value = "${Repository.basicCount}",
                unit = "条入门必学",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatValue(value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            // Expressive 加重字阶；数量统计不是代码语义，用系统默认字体
            style = MaterialTheme.typography.displaySmallEmphasized,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}
