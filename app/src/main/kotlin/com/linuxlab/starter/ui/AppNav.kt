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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.data.WallpaperStore
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.linuxlab.starter.ui.liquidglass.GlassTab
import com.linuxlab.starter.ui.liquidglass.LiquidGlassNavBar
import com.linuxlab.starter.ui.screens.AboutScreen
import com.linuxlab.starter.ui.screens.CategoryScreen
import com.linuxlab.starter.ui.screens.DesktopEnvDetailScreen
import com.linuxlab.starter.ui.screens.DesktopEnvScreen
import com.linuxlab.starter.ui.screens.DetailScreen
import com.linuxlab.starter.ui.screens.FaqScreen
import com.linuxlab.starter.ui.screens.FavoritesScreen
import com.linuxlab.starter.ui.screens.HomeScreen
import com.linuxlab.starter.ui.screens.OnboardingScreen
import com.linuxlab.starter.ui.screens.RealTerminalScreen
import com.linuxlab.starter.ui.screens.ResourceScreen
import com.linuxlab.starter.ui.screens.SearchScreen
import com.linuxlab.starter.ui.screens.ChmodScreen
import com.linuxlab.starter.ui.screens.FhsScreen
import com.linuxlab.starter.ui.screens.TerminalScreen
import com.linuxlab.starter.ui.screens.TutorialScreen

/** 底栏页签与对应路由，顺序即显示顺序 */
private val BottomTabs = listOf(
    GlassTab("速查", Icons.Outlined.Description) to "home",
    GlassTab("排错", Icons.Outlined.Build) to "faq",
    GlassTab("资源", Icons.Outlined.Link) to "resources",
    GlassTab("实战终端", Icons.Outlined.Terminal) to "terminal"
)
private val BottomRoutes = BottomTabs.map { it.second }

/** 用户自定义背景：图片铺满 + 一层可调的纯色遮罩，保证内容始终可读 */
@Composable
private fun AppBackground() {
    val version by WallpaperStore.version.collectAsState()
    val dim by WallpaperStore.dim.collectAsState()
    val bitmap = remember(version) { WallpaperStore.load() }

    // 没有自定义背景时，铺一层主题底色 —— 页面 Scaffold 是透明的，
    // 少了这层背景就不会再跟着深/浅色模式走了
    if (bitmap == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
        )
        return
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = dim))
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNav(prefs: ThemePrefs) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // 注意：destination.route 返回的是「路由模板」，带参数的页面会是 "faq?q={q}"，
    // 必须取 '?' 之前的部分才能与页签路由对上，否则排错页会被判定为不在底栏页面。
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")

    val tabIndex = BottomRoutes.indexOf(currentRoute)
    val showBottomBar = tabIndex >= 0

    // 首次进入：跳到引导页。startDestination 仍是 home，
    // 免得切页签时 popUpTo(startDestinationId) 指向一个已经出栈的页面。
    val onboardingDone by UserStore.onboardingDone.collectAsState()
    LaunchedEffect(onboardingDone) {
        if (!onboardingDone) {
            navController.navigate("onboarding") { launchSingleTop = true }
        }
    }

    // 液态玻璃的“背景来源”：把屏幕内容录制进一个 backdrop 图层，
    // 玻璃底栏再对这个图层做模糊 / 折射采样 —— 这是真实液态玻璃的前提。
    val backdrop = rememberLayerBackdrop()

    // 键盘弹出时隐藏底栏 —— 只对「实战终端」生效。
    // 终端的输入框需要独占屏幕底部（贴着键盘），被底栏压住就没法输入；
    // 但排错 / 资源页只是顶部搜索框，底栏必须保持可见，
    // 否则一点搜索框底栏就整条消失，用户根本没法再点回其它页签。
    val terminalIndex = BottomRoutes.indexOf("terminal")
    val barVisible = showBottomBar &&
        !(tabIndex == terminalIndex && WindowInsets.isImeVisible)

    // 页签回调：直接读 NavController 的当前目的地，而不是闭包里捕获的 currentRoute，
    // 这样即便被上游组件 remember 住（DampedDragAnimation 只构建一次），
    // 拿到的也永远是最新的路由，不会因旧值把跳转误判成「已在该页」而跳过。
    val onTabSelected: (Int) -> Unit = remember(navController) {
        { index ->
            val route = BottomRoutes.getOrNull(index)
            val topRoute = navController.currentDestination?.route?.substringBefore("?")
            if (route != null && route != topRoute) {
                navController.navigate(route) {
                    // 切页签时回到起始页并保存/恢复各页状态，
                    // 否则返回栈会不断堆叠，返回键行为与底栏选中态会对不上
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // 内容层：整屏铺满并延伸到玻璃底栏之下，同时被录制为 backdrop
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            // 自定义背景：铺在最底层，一起被录进 backdrop，
            // 这样液态玻璃底栏会模糊到用户自己选的图
            AppBackground()

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
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
                        bottomBarInset = if (barVisible) BOTTOM_BAR_INSET else 0.dp,
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

        // 玻璃底栏：悬浮在内容之上，不参与 backdrop 录制
        if (barVisible) {
            LiquidGlassNavBar(
                backdrop = backdrop,
                tabs = BottomTabs.map { it.first },
                selectedIndex = tabIndex,
                onSelected = onTabSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }
    }
}

/** 玻璃底栏可见时，页面底部需要预留的空间（胶囊 64dp + 上下外边距） */
val BOTTOM_BAR_INSET = 92.dp
