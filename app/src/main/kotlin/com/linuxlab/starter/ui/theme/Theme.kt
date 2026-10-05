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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** 主题模式 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * 关闭莫奈取色（或 Android 12 以下）时的兜底配色：
 * 取自 Linux / 终端气质的深绿 + 青绿主色。
 *
 * 颜色角色按 M3（m3.material.io）的完整 tonal 体系填满：
 * primary/secondary/tertiary 三族 + surface 容器五档（Lowest→Highest）+
 * surfaceDim/Bright、outlineVariant、inverse*、fixed 角色、scrim。
 * 兜底深色模式同时启用高对比容器（M3 深色色板的 contrast level 处理方式）。
 */
private val BrandGreen = Color(0xFF006C4C)
private val BrandTeal = Color(0xFF3DDC97)

private val LightColors: ColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8DF3C4),
    onPrimaryContainer = Color(0xFF002114),
    primaryFixed = Color(0xFF8DF3C4),
    primaryFixedDim = Color(0xFF57DBA4),
    onPrimaryFixed = Color(0xFF002114),
    onPrimaryFixedVariant = Color(0xFF005236),
    secondary = Color(0xFF4C6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFEDE0),
    onSecondaryContainer = Color(0xFF082018),
    secondaryFixed = Color(0xFFCFEDE0),
    secondaryFixedDim = Color(0xFFB3CCC0),
    onSecondaryFixed = Color(0xFF082018),
    onSecondaryFixedVariant = Color(0xFF354B41),
    tertiary = Color(0xFF3B5F8A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD4E3FF),
    onTertiaryContainer = Color(0xFF001C36),
    tertiaryFixed = Color(0xFFD4E3FF),
    tertiaryFixedDim = Color(0xFFAFCBFF),
    onTertiaryFixed = Color(0xFF001C36),
    onTertiaryFixedVariant = Color(0xFF27476F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surface = Color(0xFFFAFDFB),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5DF),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF6F7973),
    outlineVariant = Color(0xFFBFC9C2),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2E3230),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = Color(0xFF57DBA4),
    surfaceDim = Color(0xFFDAE0DB),
    surfaceBright = Color(0xFFFAFDFB),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F4F1),
    surfaceContainer = Color(0xFFEAEFEB),
    surfaceContainerHigh = Color(0xFFE4E9E5),
    surfaceContainerHighest = Color(0xFFDFE4E0)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = BrandTeal,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF8DF3C4),
    primaryFixed = Color(0xFF8DF3C4),
    primaryFixedDim = Color(0xFF57DBA4),
    onPrimaryFixed = Color(0xFF002114),
    onPrimaryFixedVariant = Color(0xFF005236),
    secondary = Color(0xFFB3CCC0),
    onSecondary = Color(0xFF1E352B),
    secondaryContainer = Color(0xFF354B41),
    onSecondaryContainer = Color(0xFFCFEDE0),
    secondaryFixed = Color(0xFFCFEDE0),
    secondaryFixedDim = Color(0xFFB3CCC0),
    onSecondaryFixed = Color(0xFF082018),
    onSecondaryFixedVariant = Color(0xFF354B41),
    tertiary = Color(0xFFAFCBFF),
    onTertiary = Color(0xFF0B3057),
    tertiaryContainer = Color(0xFF27476F),
    onTertiaryContainer = Color(0xFFD4E3FF),
    tertiaryFixed = Color(0xFFD4E3FF),
    tertiaryFixedDim = Color(0xFFAFCBFF),
    onTertiaryFixed = Color(0xFF001C36),
    onTertiaryFixedVariant = Color(0xFF27476F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF101413),
    onSurface = Color(0xFFE0E3E0),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFCBC3),
    outline = Color(0xFF8A968F),
    outlineVariant = Color(0xFF3F4943),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE0E3E0),
    inverseOnSurface = Color(0xFF2D3230),
    inversePrimary = Color(0xFF006C4C),
    surfaceDim = Color(0xFF101413),
    surfaceBright = Color(0xFF363A39),
    surfaceContainerLowest = Color(0xFF0B0F0E),
    surfaceContainerLow = Color(0xFF191C1B),
    surfaceContainer = Color(0xFF1D2020),
    surfaceContainerHigh = Color(0xFF272A2A),
    surfaceContainerHighest = Color(0xFF323534)
)

/**
 * Material 3 Expressive 主题（m3.material.io，material3 1.5 Expressive API）。
 *
 * 与标准 M3 的差异全部由 [MaterialExpressiveTheme] 的 token 层承担：
 * - **形状**：Expressive 形状档位 —— extraSmall 4 / small 8 / medium 12 / large 16 /
 *   extraLarge 28，另有 largeIncreased 20 / extraLargeIncreased 32 / extraExtraLarge 48 /
 *   full（胶囊）。按钮、输入框等组件默认取 full 胶囊 + 按压形变，都由主题默认值驱动；
 * - **动效**：[MotionScheme.expressive] —— 空间动效换成带弹性的 spring
 *   （default 空间 0.8/380、fast 空间 0.6/800），组件默认动画与应用内动画共用一套；
 * - **类型**：沿用 M3 标准 Typography。
 *
 * 莫奈（Monet）动态取色：Android 12+ 且开关打开时从壁纸提取主色，整站跟随；
 * 否则回退到上面的内置深绿品牌配色。
 *
 * 注意：项目卡片/列表存在「同心圆角」设计约束（内层圆角 = 外层圆角 − 间距），
 * 组件内仍按该约束以 Expressive 档位显式取值，注释里标明差值推导。
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

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography(),
        content = content
    )
}
