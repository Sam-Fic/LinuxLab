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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.linuxlab.starter.ui.ThemePrefs
import com.linuxlab.starter.ui.theme.LinuxStarterTheme
import com.linuxlab.starter.ui.theme.ThemeMode

/**
 * Compose 官方 `ui-tooling-preview` 预览。
 *
 * 约定（官方推荐做法）：
 * - 预览函数一律 `private`，不进入 release 产物；
 * - 一律有 `@Preview` 且在函数体里包一层主题，才能看到真实配色/字体；
 * - 浅色 / 深色各出一个，用来检查对比度与色彩角色是否成对；
 * - `showBackground = true` 让预览有 surface 底色，`locale` 用 zh-rCN 看中文断行。
 *
 * 这些预览只在 Android Studio 的 Design 面板或 Layout Inspector 中渲染，
 * 不参与运行时代码路径。
 */

private const val PREVIEW_NAME_PREFIX = "LinuxLab · "
private const val PREVIEW_LOCALE = "zh-rCN"

@Preview(
    name = "${PREVIEW_NAME_PREFIX}首页（浅色）",
    group = "首页",
    showBackground = true,
    locale = PREVIEW_LOCALE,
    showSystemUi = true
)
@Composable
private fun PreviewHomeLight() = previewTheme(ThemeMode.LIGHT) {
    HomeScreen(
        onSearch = {},
        onDesktops = {},
        onTutorial = {},
        onChmod = {},
        onFhs = {},
        onFavorites = {},
        onCommand = {},
        onSettings = {}
    )
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}首页（深色）",
    group = "首页",
    showBackground = true,
    locale = PREVIEW_LOCALE,
    showSystemUi = true
)
@Composable
private fun PreviewHomeDark() = previewTheme(ThemeMode.DARK) {
    HomeScreen(
        onSearch = {},
        onDesktops = {},
        onTutorial = {},
        onChmod = {},
        onFhs = {},
        onFavorites = {},
        onCommand = {},
        onSettings = {}
    )
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}搜索结果",
    group = "搜索",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewSearchLight() = previewTheme(ThemeMode.LIGHT) {
    SearchScreen(onBack = {}, onCommand = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}搜索结果（深色）",
    group = "搜索",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewSearchDark() = previewTheme(ThemeMode.DARK) {
    SearchScreen(onBack = {}, onCommand = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}命令详情",
    group = "详情",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewDetailLight() = previewTheme(ThemeMode.LIGHT) {
    DetailScreen(index = 0, onBack = {}, onCommand = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}命令详情（深色）",
    group = "详情",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewDetailDark() = previewTheme(ThemeMode.DARK) {
    DetailScreen(index = 0, onBack = {}, onCommand = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}权限计算",
    group = "工具",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewChmodLight() = previewTheme(ThemeMode.LIGHT) {
    ChmodScreen(onBack = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}权限计算（深色）",
    group = "工具",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewChmodDark() = previewTheme(ThemeMode.DARK) {
    ChmodScreen(onBack = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}文件系统树",
    group = "工具",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewFhsLight() = previewTheme(ThemeMode.LIGHT) {
    FhsScreen(onBack = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}问答",
    group = "问答",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewFaqLight() = previewTheme(ThemeMode.LIGHT) {
    FaqScreen()
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}问答（深色）",
    group = "问答",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewFaqDark() = previewTheme(ThemeMode.DARK) {
    FaqScreen()
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}资源",
    group = "资源",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewResourcesLight() = previewTheme(ThemeMode.LIGHT) {
    ResourceScreen()
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}桌面环境",
    group = "桌面环境",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewDesktopEnvLight() = previewTheme(ThemeMode.LIGHT) {
    DesktopEnvScreen(onBack = {}, onOpen = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}收藏",
    group = "收藏",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewFavoritesLight() = previewTheme(ThemeMode.LIGHT) {
    FavoritesScreen(onBack = {}, onCommand = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}引导页",
    group = "引导",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewOnboardingLight() = previewTheme(ThemeMode.LIGHT) {
    OnboardingScreen(onFinish = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}教程",
    group = "教程",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewTutorialLight() = previewTheme(ThemeMode.LIGHT) {
    TutorialScreen(onBack = {}, onCategory = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}关于（浅色）",
    group = "关于",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewAboutLight() = previewTheme(ThemeMode.LIGHT) {
    AboutScreen(prefs = previewPrefs(), onBack = {})
}

@Preview(
    name = "${PREVIEW_NAME_PREFIX}关于（深色）",
    group = "关于",
    showBackground = true,
    locale = PREVIEW_LOCALE
)
@Composable
private fun PreviewAboutDark() = previewTheme(ThemeMode.DARK) {
    AboutScreen(prefs = previewPrefs(), onBack = {})
}

/** 预览用的 ThemePrefs：本地 debug 数据，读不到也不影响渲染。 */
@Composable
private fun previewPrefs(): ThemePrefs {
    val context = LocalContext.current.applicationContext
    return remember(context) { ThemePrefs(context) }
}

/** 预览统一入口：固定主题模式，关闭动态取色，保证预览可复现。 */
@Composable
private fun previewTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    LinuxStarterTheme(themeMode = mode, useDynamicColor = false, content = content)
}
