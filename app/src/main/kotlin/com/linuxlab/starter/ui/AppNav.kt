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

package com.linuxlab.starter.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.PathEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.ui.screens.AboutScreen
import com.linuxlab.starter.ui.screens.CategoryScreen
import com.linuxlab.starter.ui.screens.ChmodScreen
import com.linuxlab.starter.ui.screens.DesktopEnvDetailScreen
import com.linuxlab.starter.ui.screens.DesktopEnvScreen
import com.linuxlab.starter.ui.screens.DetailScreen
import com.linuxlab.starter.ui.screens.FaqScreen
import com.linuxlab.starter.ui.screens.FavoritesScreen
import com.linuxlab.starter.ui.screens.FhsScreen
import com.linuxlab.starter.ui.screens.HomeScreen
import com.linuxlab.starter.ui.screens.OnboardingScreen
import com.linuxlab.starter.ui.screens.RealTerminalScreen
import com.linuxlab.starter.ui.screens.ResourceScreen
import com.linuxlab.starter.ui.screens.SearchScreen
import com.linuxlab.starter.ui.screens.TerminalScreen
import com.linuxlab.starter.ui.screens.TutorialScreen
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R

/**
 * 底栏页签：标签 + 未选中图标 + 选中图标 + 路由，顺序即显示顺序。
 *
 * 图标统一用 Filled（实心）色块版——应用整体设计遵循「色块优先、不用描边」；
 * 选中态由 ShortNavigationBar 自带的 active indicator 胶囊表达。
 */
private data class BottomTab(
    /** 标签字符串资源 id：BottomTabs 是顶层 val，不能在非 @Composable 上下文调 stringResource */
    val labelRes: Int,
    val icon: ImageVector,
    val route: String
)

private val BottomTabs = listOf(
    BottomTab(R.string.tab_quick_reference, Icons.Filled.Description, "home"),
    BottomTab(R.string.tab_troubleshoot, Icons.Filled.Build, "faq"),
    BottomTab(R.string.tab_resources, Icons.Filled.Link, "resources"),
    BottomTab(R.string.tab_terminal, Icons.Filled.Terminal, "terminal")
)

/** 底栏“顶级页面”路由，用于区分「切页签」和「进/出层级页面」。 */
private val BottomTabRoutes = BottomTabs.map { it.route }.toSet()

/**
 * 底栏页签切换使用 Material fade-through：
 * 旧内容快速淡出并缩到 92%，新内容稍后淡入并放大回 100%。
 * 左右层级滑动只保留给「上一级/下一级」页面，不用于顶级页签之间。
 */
private val BottomTabEnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = 180,
        delayMillis = 90,
        easing = LinearOutSlowInEasing
    )
) + scaleIn(
    animationSpec = tween(
        durationMillis = 180,
        delayMillis = 90,
        easing = LinearOutSlowInEasing
    ),
    initialScale = 0.92f
)

private val BottomTabExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = 90,
        easing = LinearOutSlowInEasing
    )
) + scaleOut(
    animationSpec = tween(
        durationMillis = 90,
        easing = LinearOutSlowInEasing
    ),
    targetScale = 0.92f
)

/** 当前转场是否发生在两个底栏顶级页签之间。 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isBottomTabSwitch(): Boolean {
    val from = initialState.destination.route?.substringBefore("?")
    val to = targetState.destination.route?.substringBefore("?")
    return from != null && to != null && from in BottomTabRoutes && to in BottomTabRoutes
}

/** 与 Android 系统 Activity 默认转场一致的位移/时长（@android:anim/activity_*）。 */
private const val ACTIVITY_TRANSITION_OFFSET_DP = 96
private const val ACTIVITY_TRANSITION_DURATION_MS = 450
private const val ACTIVITY_FADE_IN_DURATION_MS = 83
private const val ACTIVITY_FADE_IN_DELAY_MS = 50
private const val ACTIVITY_FADE_OUT_DURATION_MS = 83
private const val ACTIVITY_FADE_OUT_DELAY_MS = 35

/**
 * 系统 `@android:interpolator/fast_out_extra_slow_in` 的两段三次贝塞尔路径。
 * 原生 Activity 转场用它做「快速起步、极慢收尾」，而不是 Expressive spring，
 * 所以不会出现横向回弹/过冲。
 */
private val ActivityTransitionEasing = PathEasing(
    Path().apply {
        moveTo(0f, 0f)
        cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f)
        cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)
    }
)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AppNav(prefs: ThemePrefs) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // 注意：destination.route 返回的是「路由模板」，带参数的页面会是 "faq?q={q}"，
    // 必须取 '?' 之前的部分才能与页签路由对上，否则排错页会被判定为不在底栏页面。
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")
    val currentTab = BottomTabs.indexOfFirst { it.route == currentRoute }
    val showBottomBar = currentTab >= 0

    // 首次进入：跳到引导页。startDestination 仍是 home，
    // 免得切页签时 popUpTo(startDestinationId) 指向一个已经出栈的页面。
    val onboardingDone by UserStore.onboardingDone.collectAsState()
    LaunchedEffect(onboardingDone) {
        if (!onboardingDone) {
            navController.navigate("onboarding") { launchSingleTop = true }
        }
    }

    // 键盘弹出时隐藏底栏 —— 只对「实战终端」生效。
    // 终端的输入框需要独占屏幕底部（贴着键盘），被底栏压住就没法输入；
    // 但排错 / 资源页只是顶部搜索框，底栏必须保持可见，
    // 否则一点搜索框底栏就整条消失，用户根本没法再点回其它页签。
    val imeVisible = WindowInsets.isImeVisible
    val barVisible = showBottomBar &&
        !(currentTab == BottomTabs.indexOfFirst { it.route == "terminal" } && imeVisible)


    // 页签切换：回到起始页并保存/恢复各页状态，
    // 否则返回栈会不断堆叠，返回键行为与底栏选中态会对不上
    val onTabSelected: (Int) -> Unit = remember(navController) {
        { index ->
            val route = BottomTabs.getOrNull(index)?.route
            val topRoute = navController.currentDestination?.route?.substringBefore("?")
            if (route != null && route != topRoute) {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    // 应用壳：唯一的底栏挂在最外层 Scaffold 上（M3 标准做法）。
    // - NavigationBar 自行处理手势导航条（小白条）的 insets，内容天然避让；
    // - contentWindowInsets 置零：顶部 insets 交给各页面自己的 TopAppBar，
    //   底部 insets 由 innerPadding 统一下发；
    // - consumeWindowInsets 把底栏占掉的部分标记为已消费，
    //   内层页面的 Scaffold 就不会对手势条二次避让。
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            if (barVisible) {
                // Material 3 Expressive 官方短导航栏：选中项是带形状指示器的胶囊，
                // 未选中项只留图标，比传统 NavigationBar 更紧凑、更 expressive。
                ShortNavigationBar(
                    arrangement = ShortNavigationBarArrangement.EqualWeight
                ) {
                    BottomTabs.forEachIndexed { index, tab ->
                        val selected = index == currentTab
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = { onTabSelected(index) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 页面切换分两类：
        // 1. 层级页面（首页 -> 详情 / 详情返回等）对齐 Android 系统 Activity 默认转场：
        //    - 进入：新页从右侧 96dp 处滑入，并在 50ms 后快速淡入（83ms）；
        //    - 退出：旧页只向左让位 96dp，保持完全不透明，负责衬在新页后面；
        //    - 返回：上一级页从左侧 96dp 处滑回，当前页向右让位 96dp 并快速淡出；
        //    - 位移用系统 fast_out_extra_slow_in 路径，避免 Expressive 空间弹簧的横向回弹。
        // 2. 底栏顶级页签之间：走 Material fade-through，不左右滑动。
        val pageOffsetPx = with(LocalDensity.current) {
            ACTIVITY_TRANSITION_OFFSET_DP.dp.roundToPx()
        }
        val pageSlideSpec = tween<IntOffset>(
            durationMillis = ACTIVITY_TRANSITION_DURATION_MS,
            easing = ActivityTransitionEasing
        )
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = {
                if (isBottomTabSwitch()) {
                    BottomTabEnterTransition
                } else {
                    slideInHorizontally(
                        animationSpec = pageSlideSpec,
                        initialOffsetX = { pageOffsetPx }
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = ACTIVITY_FADE_IN_DURATION_MS,
                            delayMillis = ACTIVITY_FADE_IN_DELAY_MS,
                            easing = LinearEasing
                        )
                    )
                }
            },
            exitTransition = {
                if (isBottomTabSwitch()) {
                    BottomTabExitTransition
                } else {
                    slideOutHorizontally(
                        animationSpec = pageSlideSpec,
                        targetOffsetX = { -pageOffsetPx }
                    )
                }
            },
            popEnterTransition = {
                if (isBottomTabSwitch()) {
                    BottomTabEnterTransition
                } else {
                    slideInHorizontally(
                        animationSpec = pageSlideSpec,
                        initialOffsetX = { -pageOffsetPx }
                    )
                }
            },
            popExitTransition = {
                if (isBottomTabSwitch()) {
                    BottomTabExitTransition
                } else {
                    slideOutHorizontally(
                        animationSpec = pageSlideSpec,
                        targetOffsetX = { pageOffsetPx }
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = ACTIVITY_FADE_OUT_DURATION_MS,
                            delayMillis = ACTIVITY_FADE_OUT_DELAY_MS,
                            easing = LinearEasing
                        )
                    )
                }
            }
        ) {

            composable("onboarding") {
                OnboardingScreen(
                    onFinish = {
                        UserStore.setOnboardingDone()
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onSearch = { navController.navigate("search") },
                    onCommand = { index -> navController.navigate("detail/$index") },
                    onDesktops = { navController.navigate("desktops") },
                    onTutorial = { navController.navigate("tutorial") },
                    onChmod = { navController.navigate("chmod") },
                    onFhs = { navController.navigate("fhs") },
                    onFavorites = { navController.navigate("favorites") },
                    onSettings = { navController.navigate("about") }
                )
            }

            composable(
                route = "faq?q={q}",
                arguments = listOf(navArgument("q") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                FaqScreen(initialQuery = backStackEntry.arguments?.getString("q").orEmpty())
            }

            composable("desktops") {
                DesktopEnvScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { id -> navController.navigate("desktops/$id") }
                )
            }

            composable(
                route = "desktops/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                DesktopEnvDetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("tutorial") {
                TutorialScreen(
                    onBack = { navController.popBackStack() },
                    onCategory = { id -> navController.navigate("category/$id") }
                )
            }

            composable("chmod") {
                ChmodScreen(onBack = { navController.popBackStack() })
            }

            composable("fhs") {
                FhsScreen(onBack = { navController.popBackStack() })
            }

            composable("favorites") {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onCommand = { index -> navController.navigate("detail/$index") }
                )
            }

            composable("resources") {
                ResourceScreen()
            }

            composable("terminal") {
                RealTerminalScreen(
                    onFallbackToSandbox = {
                        navController.navigate("sandbox") { launchSingleTop = true }
                    }
                )
            }

            composable("sandbox") {
                TerminalScreen()
            }

            composable("search") {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onCommand = { index -> navController.navigate("detail/$index") },
                    onFaq = { q -> navController.navigate("faq?q=${java.net.URLEncoder.encode(q, "UTF-8")}") }
                )
            }

            composable(
                route = "category/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                CategoryScreen(
                    groupId = id,
                    onBack = { navController.popBackStack() },
                    onCommand = { index -> navController.navigate("detail/$index") }
                )
            }

            composable(
                route = "detail/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                DetailScreen(
                    index = index,
                    onBack = { navController.popBackStack() },
                    onCommand = { next -> navController.navigate("detail/$next") }
                )
            }

            composable("about") {
                AboutScreen(
                    prefs = prefs,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
