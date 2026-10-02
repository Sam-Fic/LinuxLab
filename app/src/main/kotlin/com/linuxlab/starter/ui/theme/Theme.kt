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

package com.linuxlab.starter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** 主题模式 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * 关闭莫奈取色（或 Android 12 以下）时的兜底配色：
 * 取自 Linux / 终端气质的深绿 + 青绿主色。
 */
private val BrandGreen = Color(0xFF006C4C)
private val BrandTeal = Color(0xFF3DDC97)

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8DF3C4),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFEDE0),
    onSecondaryContainer = Color(0xFF082018),
    tertiary = Color(0xFF3B5F8A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD4E3FF),
    onTertiaryContainer = Color(0xFF001C36),
    surface = Color(0xFFFAFDFB),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5DF),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF6F7973),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = BrandTeal,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF8DF3C4),
    secondary = Color(0xFFB3CCC0),
    onSecondary = Color(0xFF1E352B),
    secondaryContainer = Color(0xFF354B41),
    onSecondaryContainer = Color(0xFFCFEDE0),
    tertiary = Color(0xFFAFCBFF),
    onTertiary = Color(0xFF0B3057),
    tertiaryContainer = Color(0xFF27476F),
    onTertiaryContainer = Color(0xFFD4E3FF),
    surface = Color(0xFF101413),
    onSurface = Color(0xFFE0E3E0),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFCBC3),
    outline = Color(0xFF8A968F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val AppTypography = Typography()

/**
 * Material 3 标准形状体系（五个档位）。
 *
 * 与 M3 默认 Shapes 取值一致：extraSmall 4 / small 8 / medium 12 / large 16 / extraLarge 28。
 * 显式声明并注入 MaterialTheme，让所有标准组件（按钮、卡片、输入框、FAB 等）
 * 都能通过 `MaterialTheme.shapes` 语义化取形状，而不是散落的硬编码圆角。
 *
 * 注意：项目卡片/列表存在「同心圆角」设计约束（内层圆角 = 外层圆角 − 间距），
 * 自定义圆角仍按该约束在组件内显式指定，不受本档位影响。
 */
val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/**
 * Material 3 + 莫奈（Monet）动态取色主题。
 *
 * - Android 12（API 31）及以上且开启了「动态取色」时，
 *   通过 dynamicLightColorScheme / dynamicDarkColorScheme 从用户壁纸提取主色，
 *   整站控件（按钮、卡片、搜索栏、导航、状态栏）自动跟随壁纸变色。
 * - 低版本或用户关闭开关时，回退到内置的深绿品牌配色。
 */
@Composable
fun LinuxStarterTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamicColor && supportsDynamic && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor && supportsDynamic -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
