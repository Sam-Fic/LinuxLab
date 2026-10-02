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

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.FilledButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.data.UserStore
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val desc: String,
    val points: List<String>
)

private fun onboardingPages() = listOf(
    OnboardingPage(
        icon = Icons.Outlined.Terminal,
        title = "欢迎入门 Linux",
        subtitle = "Welcome to Linux",
        desc = "从这里开始，\n一步步认识命令行的世界。",
        points = listOf(
            "不需要任何基础，跟着走就行",
            "每条命令都有中文说明和可复制的示例",
            "随时可以在终端里真刀真枪地练"
        )
    ),
    OnboardingPage(
        icon = Icons.Outlined.RocketLaunch,
        title = "为什么要学 Linux",
        subtitle = "Why Linux",
        desc = "命令行不是过时的东西，\n而是直接与机器对话的最快方式。",
        points = listOf(
            "服务器与云上绝大多数实例都在跑 Linux",
            "Android、路由器、智能设备的底层也是 Linux",
            "会命令行，就多一种真正掌控整机的能力"
        )
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Outlined.MenuBook,
        title = "这个 App 能给你什么",
        subtitle = "What you get",
        desc = "从第一条命令开始，\n到能在真终端里动手操作。",
        points = listOf(
            "221 条常用命令：中英对照 + 示例 + 收藏",
            "真正的终端：真实 Linux 用户态，可装软件包",
            "图解教程：目录结构、权限计算、桌面环境"
        )
    ),
    OnboardingPage(
        icon = Icons.Outlined.AutoAwesome,
        title = "准备好了",
        subtitle = "You're all set",
        desc = "按这个顺序学，见效最快。\n全部内容离线可用，无需账号。",
        points = listOf(
            "先刷命令速查，认识最常用的那些命令",
            "再到实战终端里亲手敲一遍",
            "最后看目录结构与权限，补齐底层认知"
        )
    )
)

/**
 * 首次进入应用的引导：三页，说明「为什么学 Linux」＋「能给你什么」。
 * 动画都做得很轻：内容依次滑入、图标缓慢呼吸、指示点伸缩，
 * 背景两团渐变光斑缓慢漂浮 —— 只做点缀，不抢内容。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = onboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val isLast = pagerState.currentPage == pages.size - 1

    // 透明 Scaffold：由外层 AppNav 铺壁纸/主题底色，引导页保持通透（合理例外）
    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FloatingGlow()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // 跳过
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onFinish) {
                        Text("跳过", style = MaterialTheme.typography.labelLarge)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    PageContent(
                        page = pages[page],
                        active = pagerState.currentPage == page
                    )
                }

                // 指示点：当前页那条会拉长
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val selected = index == pagerState.currentPage
                        val w by animateDpAsState(
                            targetValue = if (selected) 22.dp else 7.dp,
                            animationSpec = tween(240),
                            label = "dot$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = w, height = 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) cs.primary
                                    else cs.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                        )
                    }
                }

                // 主按钮（M3 FilledButton：默认全圆角、primary/onPrimary 配色）
                FilledButton(
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = if (isLast) "开始使用" else "继续",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                TextButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "离线可用 · 无账号 · 无追踪",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage, active: Boolean) {
    val cs = MaterialTheme.colorScheme

    // 进入该页时依次播放入场动画；离开后不复位，避免滑走时内容凭空消失
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(active) {
        if (active) entered = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseValue"
        )

        // 图标：渐变圆底 + 呼吸缩放
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = cs.primaryContainer,
            modifier = Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .reveal(entered, delay = 0)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(cs.primaryContainer, cs.tertiaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = cs.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.reveal(entered, delay = 70)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.labelLarge,
            color = cs.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.reveal(entered, delay = 110)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = page.desc,
            style = MaterialTheme.typography.bodyLarge,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.reveal(entered, delay = 150)
        )

        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            page.points.forEachIndexed { index, text ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cs.surfaceContainerHigh.copy(alpha = 0.75f))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .reveal(entered, delay = 200 + index * 70),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(cs.primary)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface
                    )
                }
            }
        }
    }
}

/** 入场：淡入 + 轻微上移，delay 决定第几个出场 */
@Composable
private fun Modifier.reveal(entered: Boolean, delay: Int): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 320, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "reveal$delay"
    )
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 26f
    }
}

/** 背景两团缓慢漂浮的渐变光斑，纯装饰 */
@Composable
private fun FloatingGlow() {
    val cs = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "glow")
    val shift by transition.animateFloat(
        initialValue = -26f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowShift"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = shift.dp)
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(cs.primary.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 420f
                    )
                )
        )
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = (-shift).dp)
                .size(240.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = (-120).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(cs.tertiary.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 460f
                    )
                )
        )
    }
}
