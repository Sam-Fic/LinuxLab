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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.HomeSection
import com.linuxlab.starter.data.Repository
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.model.Command
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.rememberCopyAction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** 网格里每列的数量 */
private const val COLUMNS = 2

/** 网格卡片统一高度：两列并排时高度必须一致，否则让位会错位 */
private val GRID_CARD_HEIGHT = 132.dp

// ---------------------------------------------------------------- 首页

/**
 * 首页：搜索入口 → 今日推荐大卡 → 两列可拖动排序的板块网格 → 统计条。
 *
 * 用的是 Compose 官方的列表重排做法：
 * - 网格里的每一项用 **卡片 id 作为 key**，所以顺序变化时 LazyGrid 只是把同一项
 *   挪个位置，不会销毁重建 —— 手势（pointerInput）因此能一直握在手指下那张卡上；
 * - 位移动画交给框架的 `Modifier.animateItem()`，让位是列表自己做的，
 *   不再靠我手工算偏移量（之前那套偏移量方案有各种坐标/时序上的坑）；
 * - 拖动中的卡片单独画在最上层的覆盖层里，位置 = 抓起处 + 手指位移。
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
    val daily = Repository.dailyCommand()
    val copyAction = rememberCopyAction()
    val favorites by UserStore.favorites.collectAsState()
    val savedOrder by UserStore.sectionOrder.collectAsState()
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 参与排序的板块（不含今日推荐，它是整行大卡）
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
     * 列表项位移动画（让位）：
     * - 拖动中：稍慢一点（约 300ms）并带轻微回弹，看起来是"挪开"而不是"闪开"；
     * - 松手后：立刻换成瞬时弹簧收尾。因为拖动快时卡片会落在手指后面，
     *   不在松手时收尾的话，它又会把剩下那段滑完（就是之前的"从远处滑来"）。
     */
    val placementSpec = if (draggingId != null) {
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 900f
        )
    } else {
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
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

    /**
     * 松手：顺序定稿写入存储，覆盖层立刻消失、卡片当场归位到它所在的格子，
     * 回弹用缩放来做（像被吸进格子里），**不做任何位移动画** ——
     * 这样就不存在"从远处滑来"的可能。
     */
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
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                                    imageVector = Icons.Outlined.Terminal,
                                    contentDescription = null,
                                    modifier = Modifier.padding(7.dp),
                                    tint = cs.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(10.dp))
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
                            Icon(Icons.Outlined.Palette, contentDescription = "外观设置")
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
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 搜索入口（整行）
                item(span = { GridItemSpan(maxLineSpan) }) {
                    // M3 标准搜索入口组件（material3 SearchBar，点击跳到搜索页）
                    SearchBar(
                        query = "",
                        onQueryChange = {},
                        onSearch = { onSearch() },
                        active = false,
                        onActiveChange = { if (it) onSearch() },
                        placeholder = { Text("搜索命令、参数或示例…") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }

                // 今日推荐：整行大卡
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DailyCard(
                        command = daily,
                        onCopy = copyAction,
                        onClick = { if (clickAllowed()) onCommand(daily.index) }
                    )
                }

                // 可排序的板块：key 用 id，顺序变化时列表只是挪位置，不会重建
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

                // 统计条（整行）
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsRow()
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        }

        // 拖动中的卡片：画在所有内容之上，位置只跟手指走
        val dragId = draggingId
        if (dragId != null) {
            val scale by animateFloatAsState(
                targetValue = 1.05f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "dragScale"
            )
            val elevation by animateFloatAsState(
                targetValue = 22f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
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
    LaunchedEffect(dropped) {
        if (dropped) {
            bounce.snapTo(0.92f)
            bounce.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
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
            icon = Icons.Outlined.Computer,
            title = "桌面环境教程",
            desc = "9 个桌面 · 图解面板与快捷键",
            container = cs.secondaryContainer,
            content = cs.onSecondaryContainer
        )

        HomeSection.TUTORIAL.id -> SectionSpec(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            title = "终端命令教程",
            desc = "${Repository.categoryCount} 类 ${Repository.totalCount} 条 · 含学习顺序",
            container = cs.tertiaryContainer,
            content = cs.onTertiaryContainer
        )

        HomeSection.FAVORITES.id -> SectionSpec(
            icon = Icons.Outlined.Star,
            title = "我的收藏",
            desc = if (favCount == 0) "还没有收藏 · 点星标添加" else "已收藏 $favCount 条命令",
            container = cs.primaryContainer,
            content = cs.onPrimaryContainer
        )

        HomeSection.CHMOD.id -> SectionSpec(
            icon = Icons.Outlined.Lock,
            title = "权限计算器",
            desc = "勾选 rwx → 755 / 644",
            container = cs.tertiaryContainer,
            content = cs.onTertiaryContainer
        )

        else -> SectionSpec(
            icon = Icons.Outlined.FolderOpen,
            title = "FHS 目录结构",
            desc = "${Repository.fhsCount} 个目录点开看用途",
            container = cs.surfaceContainerHighest,
            content = cs.onSurface
        )
    }
}

@Composable
private fun SectionCard(spec: SectionSpec, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_CARD_HEIGHT),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = spec.container)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 同心圆角：外卡 20.dp 与内图标块间距 12.dp，圆角差 20-12=8.dp
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = spec.content.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = null,
                    tint = spec.content,
                    modifier = Modifier.padding(9.dp)
                )
            }
            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleSmall,
                color = spec.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = spec.desc,
                style = MaterialTheme.typography.bodySmall,
                color = spec.content.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
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
        modifier = Modifier
            .fillMaxWidth()
            // 自身不再加左右内边距：网格的 contentPadding(14dp) 已经决定了
            // 左右边缘，这样大卡与下方两列卡片的外沿正好对齐
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cs.primaryContainer)
    ) {
        Box {
            // 轻微渐变，让每日推荐更有层次（颜色全部来自莫奈调色板）
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(cs.primaryContainer, cs.tertiaryContainer)
                        )
                    )
            )
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = cs.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "今日推荐命令",
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
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = command.zh,
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = command.en,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onPrimaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (command.examples.isNotEmpty()) {
                    // 同心圆角：外卡 32.dp（shapes.extraLarge）与代码块间距 18.dp，圆角差 32-18=14.dp
                    CodeBlock(
                        code = command.examples.first().code,
                        modifier = Modifier.padding(top = 14.dp),
                        onCopy = onCopy,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 统计条

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = "${Repository.totalCount}",
            label = "条命令",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${Repository.categoryCount}",
            label = "个分类",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${Repository.basicCount}",
            label = "条入门必学",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        // 统计展示容器：形状走主题 token（shapes.large）
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
