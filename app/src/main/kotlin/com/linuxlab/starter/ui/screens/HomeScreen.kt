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

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.linuxlab.starter.R
import com.linuxlab.starter.data.HomeSection
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.ui.components.BlobContainer
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.theme.Spacing
import kotlin.math.roundToInt

/** 网格里每列的数量 */
private const val COLUMNS = 2

/** 网格卡片统一高度：两列并排时高度必须一致，否则让位会错位 */
private val GRID_CARD_HEIGHT = 132.dp

// ---------------------------------------------------------------- 首页

/**
 * 首页：品牌区 → 搜索入口 → 今日推荐翻卡（官方多浏览轮播，多条命令）
 * → 两列可拖动排序的板块宫格 → 全宽统计带。
 *
 * - 翻卡用的是官方 Carousel（HorizontalMultiBrowseCarousel），宽随屏 75% 自适应、
 *   高由内容推导（随系统字体缩放）；
 * - 宫格用的是 Compose 官方的列表重排做法：每一项用卡片 id 作为 key，
 *   顺序变化时 LazyGrid 只是把同一项挪个位置；让位动画交给
 *   `Modifier.animateItem()`，拖动中的卡片单独画在最上层覆盖层里。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val dailies = remember { Repository.dailyCommands() }
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
    val savedOrder by UserStore.sectionOrder.collectAsState()
    val cs = MaterialTheme.colorScheme
    val density = LocalDensity.current

    // 参与排序的板块（不含今日推荐，它是整行翻卡）
    val sortableIds = remember {
        HomeSection.entries.map { it.id }.filter { it != HomeSection.DAILY.id }
    }
    val gridOrder = remember { mutableStateListOf<String>() }
    LaunchedEffect(savedOrder) {
        val desired = savedOrder
            .filter { it != HomeSection.DAILY.id }
            .plus(sortableIds)
            .distinct()
        if (gridOrder.toList() != desired) {
            gridOrder.clear()
            gridOrder.addAll(desired)
        }
    }
    val saveOrder: () -> Unit = {
        UserStore.setSectionOrder(listOf(HomeSection.DAILY.id) + gridOrder.toList())
    }

    val gridState = rememberLazyGridState()
    /** 网格整体在屏幕上的位置，用来把 item 的相对坐标换算成屏幕坐标 */
    var gridRoot by remember { mutableStateOf(Offset.Zero) }
    var cardW by remember { mutableFloatStateOf(0f) }
    var cardH by remember { mutableFloatStateOf(0f) }

    /** 某张卡片当前所在格子的屏幕左上角坐标（取自列表的真实布局结果） */
    fun cellOf(id: String): Offset? {
        val info = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return null
        return gridRoot + Offset(info.offset.x.toFloat(), info.offset.y.toFloat())
    }

    // ---- 拖动状态 ----
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    /** 抓起时那张卡片的屏幕位置 */
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    /** 刚落位的那张：给它一个缩放回弹，取代位移式回弹 */
    var droppedId by remember { mutableStateOf<String?>(null) }
    var suppressClickUntil by remember { mutableLongStateOf(0L) }
    val clickAllowed: () -> Boolean = { System.currentTimeMillis() > suppressClickUntil }

    val overlayPos: Offset = dragStart + Offset(dragX, dragY)

    /**
     * 列表项位移动画（让位），规格全部取自 MaterialTheme.motionScheme，不再手写 stiffness：
     * - 拖动中：慢空间规格，让位看起来是"挪开"而不是"闪开"；
     * - 松手后：快空间规格立刻收尾。
     */
    val motion = MaterialTheme.motionScheme
    val placementSpec = if (draggingId != null) {
        motion.slowSpatialSpec<IntOffset>()
    } else {
        motion.fastSpatialSpec<IntOffset>()
    }

    /** 拖动中：覆盖层跟手；手指压到哪一格，顺序就插到那一格，列表自己让位 */
    val onDrag: (Float, Float) -> Unit = { dx, dy ->
        val id = draggingId
        if (id != null) {
            dragX += dx
            dragY += dy
            val center = dragStart + Offset(dragX + cardW / 2f, dragY + cardH / 2f)
            val cells = gridOrder.map { cellOf(it) }
            val target = nearestIndex(center, cells, cardW, cardH)
            val cur = gridOrder.indexOf(id)
            if (target >= 0 && target != cur) {
                gridOrder.remove(id)
                gridOrder.add(target.coerceIn(0, gridOrder.size), id)
            }
        }
    }

    /** 松手：顺序定稿写入存储，覆盖层消失、卡片当场归位（缩放回弹） */
    val endDrag: () -> Unit = {
        val id = draggingId
        if (id != null) {
            saveOrder()
            droppedId = id
            draggingId = null
            suppressClickUntil = System.currentTimeMillis() + 300L
        }
    }

    Box(Modifier.fillMaxSize()) {
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(COLUMNS),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .onGloballyPositioned { gridRoot = it.positionInRoot() },
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // 搜索入口（整行）
                item(span = { GridItemSpan(maxLineSpan) }) {
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

                // 今日推荐翻卡（整行）
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CommandCarousel(commands = dailies, onCommand = onCommand)
                }

                // 可排序的板块宫格：key 用 id，顺序变化时列表只是挪位置，不会重建
                items(
                    items = gridOrder.toList(),
                    key = { it },
                    span = { GridItemSpan(1) }
                ) { id ->
                    SortableCard(
                        modifier = Modifier
                            .animateItem(placementSpec = placementSpec)
                            .height(GRID_CARD_HEIGHT),
                        id = id,
                        dragging = draggingId == id,
                        dropped = droppedId == id,
                        onBounceEnd = { if (droppedId == id) droppedId = null },
                        onMeasured = { w, h ->
                            if (w > 0f) cardW = w
                            if (h > 0f) cardH = h
                        },
                        onDragStart = {
                            dragStart = cellOf(id) ?: Offset.Zero
                            dragX = 0f
                            dragY = 0f
                            draggingId = id
                        },
                        onDragEnd = endDrag,
                        onDrag = onDrag
                    ) {
                        SectionCard(
                            spec = sectionSpec(id = id, favCount = favorites.size),
                            onClick = {
                                if (!clickAllowed()) return@SectionCard
                                when (id) {
                                    HomeSection.DESKTOP.id -> onDesktops()
                                    HomeSection.TUTORIAL.id -> onTutorial()
                                    HomeSection.FAVORITES.id -> onFavorites()
                                    HomeSection.CHMOD.id -> onChmod()
                                    HomeSection.FHS.id -> onFhs()
                                }
                            }
                        )
                    }
                }

                // 统计带（整行）
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        StatsBand()
                        Spacer(modifier = Modifier.height(96.dp)) // 派生值：顶栏收起位移 + 安全余量
                    }
                }
            }
        }

        // 拖动中的卡片：画在所有内容之上，位置只跟手指走
        val dragId = draggingId
        if (dragId != null) {
            // 拖拽是高频跟手交互，用 motionScheme 的快速空间规格
            val scale by animateFloatAsState(
                targetValue = 1.05f,
                animationSpec = motion.fastSpatialSpec(),
                label = "dragScale"
            )
            val elevation by animateFloatAsState(
                targetValue = 22f,
                animationSpec = motion.fastSpatialSpec(),
                label = "dragElevation"
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(overlayPos.x.roundToInt(), overlayPos.y.roundToInt()) }
                    .size(
                        width = with(density) { cardW.toDp() },
                        height = with(density) { cardH.toDp() }
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = elevation
                    }
            ) {
                SectionCard(
                    spec = sectionSpec(id = dragId, favCount = favorites.size),
                    onClick = {}
                )
            }
        }
    }
}

/** 手指中心离哪个格子的中心最近 */
private fun nearestIndex(
    center: Offset,
    cells: List<Offset?>,
    cardW: Float,
    cardH: Float
): Int {
    if (cardW <= 0f || cardH <= 0f) return -1
    var best = -1
    var bestD = Float.MAX_VALUE
    cells.forEachIndexed { i, p ->
        if (p != null) {
            val c = p + Offset(cardW / 2f, cardH / 2f)
            val d = (c - center).getDistanceSquared()
            if (d < bestD) {
                bestD = d
                best = i
            }
        }
    }
    return best
}

@Composable
private fun SortableCard(
    modifier: Modifier = Modifier,
    id: String,
    dragging: Boolean,
    /** 刚落位：先缩到 0.92 再弹回 1，形成"吸进格子"的回弹 */
    dropped: Boolean,
    onBounceEnd: () -> Unit,
    onMeasured: (Float, Float) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    LaunchedEffect(dragging) {
        if (dragging) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    val bounce = remember { Animatable(1f) }
    // 落位回弹用 motionScheme 的默认空间规格（官方 token）
    val bounceSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    LaunchedEffect(dropped) {
        if (dropped) {
            bounce.snapTo(0.92f)
            bounce.animateTo(targetValue = 1f, animationSpec = bounceSpec)
            onBounceEnd()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                onMeasured(it.size.width.toFloat(), it.size.height.toFloat())
            }
            .pointerInput(id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { _, amount -> onDrag(amount.x, amount.y) }
                )
            }
            .graphicsLayer {
                alpha = if (dragging) 0.25f else 1f
                scaleX = bounce.value
                scaleY = bounce.value
            }
    ) {
        content()
    }
}

// ---------------------------------------------------------------- 今日推荐翻卡

/** 今日推荐翻卡：官方多浏览轮播，一组按天轮换的命令卡（点击进入命令详情） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandCarousel(
    commands: List<Command>,
    onCommand: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    // 高度自适应：标签行 + 命令名 + 中文两行 + 英文一行 + 上下内边距。
    // 行高是 sp→dp，随系统字体缩放，大字体下轮播自动长高、文字永不裁切。
    val itemHeight = with(LocalDensity.current) {
        val labelLine = MaterialTheme.typography.labelLarge.lineHeight.toDp()
        val nameLine = MaterialTheme.typography.headlineSmall.lineHeight.toDp()
        val zhLine = MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2
        val enLine = MaterialTheme.typography.bodySmall.lineHeight.toDp()
        Spacing.lg * 2 + labelLine + Spacing.sm + nameLine + Spacing.xs + zhLine + Spacing.xs + enLine
    }
    // 宽度自适应：主卡占可用宽度 75%（220–400dp 防极端屏宽）
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val preferredItemWidth = (maxWidth * 0.75f).coerceIn(220.dp, 400.dp)
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState(itemCount = { commands.size }),
            preferredItemWidth = preferredItemWidth,
            itemSpacing = Spacing.md,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        ) { index ->
            val command = commands[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // 官方 maskClip：按轮播遮罩裁切；首页大容器统一 extraLargeIncreased(32dp)
                    .maskClip(MaterialTheme.shapes.extraLargeIncreased)
                    .background(cs.primaryContainer)
                    .clickable { onCommand(command.index) }
                    .padding(Spacing.lg)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BlobContainer(
                        shape = MaterialShapes.Cookie9Sided,
                        color = cs.onPrimaryContainer.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = cs.onPrimaryContainer,
                            modifier = Modifier.padding(Spacing.sm)
                        )
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = stringResource(R.string.section_daily_command),
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onPrimaryContainer
                    )
                }
                Text(
                    text = command.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = cs.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
                Text(
                    text = command.zh,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                Text(
                    text = command.en,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 分区宫格

private data class SectionSpec(
    val icon: ImageVector,
    val title: String,
    val desc: String,
    /** 图标底板的官方有机形状（各分区不同） */
    val shape: RoundedPolygon
)

@Composable
private fun sectionSpec(id: String, favCount: Int): SectionSpec {
    val cs = MaterialTheme.colorScheme
    return when (id) {
        HomeSection.DESKTOP.id -> SectionSpec(
            icon = Icons.Filled.Computer,
            shape = MaterialShapes.Slanted,
            title = stringResource(R.string.section_desktop_tutorial),
            desc = "9 个桌面 · 图解面板与快捷键",
        )

        HomeSection.TUTORIAL.id -> SectionSpec(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            shape = MaterialShapes.Pill,
            title = stringResource(R.string.section_terminal_tutorial),
            desc = "${Repository.categoryCount} 类 ${Repository.totalCount} 条 · 含学习顺序",
        )

        HomeSection.FAVORITES.id -> SectionSpec(
            icon = Icons.Filled.Star,
            shape = MaterialShapes.Heart,
            title = stringResource(R.string.section_favorites),
            desc = if (favCount == 0) "还没有收藏 · 点星标添加" else "已收藏 $favCount 条命令",
        )

        HomeSection.CHMOD.id -> SectionSpec(
            icon = Icons.Filled.Lock,
            shape = MaterialShapes.PuffyDiamond,
            title = stringResource(R.string.section_chmod),
            desc = "勾选 rwx → 755 / 644",
        )

        else -> SectionSpec(
            icon = Icons.Filled.FolderOpen,
            shape = MaterialShapes.ClamShell,
            title = stringResource(R.string.section_fhs),
            desc = "${Repository.fhsCount} 个目录点开看用途",
        )
    }
}

@Composable
private fun SectionCard(spec: SectionSpec, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_CARD_HEIGHT),
        // 内容卡统一圆角档：largeIncreased(20dp)；
        // 卡体统一中性 surfaceContainerHigh——深色莫奈下不再出现整片亮色卡，
        // 每区的颜色身份收进 38dp 图标块（accent 配对色）
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // 图标底板：官方有机形状（各分区不同），颜色统一 secondaryContainer
            // （与分类卡图标块同一约定）；有机形状不受同心圆角约束
            BlobContainer(
                shape = spec.shape,
                color = cs.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = null,
                    tint = cs.onSecondaryContainer,
                    modifier = Modifier.padding(Spacing.sm)
                )
            }
            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Text(
                text = spec.desc,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs)
            )
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
        // 首页大容器圆角统一档：extraLargeIncreased(32dp)
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
            // Expressive 加重字阶 + 等宽字体：统计数字更实、更有终端气质
            style = MaterialTheme.typography.displaySmallEmphasized.copy(
                fontFamily = FontFamily.Monospace
            ),
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
