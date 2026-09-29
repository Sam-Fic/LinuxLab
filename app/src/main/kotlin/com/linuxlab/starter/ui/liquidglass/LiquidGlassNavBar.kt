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

package com.linuxlab.starter.ui.liquidglass

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.kyant.backdrop.Backdrop

/**
 * 当前实际生效的配色是不是深色。
 *
 * 不能直接用 isSystemInDarkTheme()：App 内的「外观设置」可以强制浅色 / 深色，
 * 那时系统设置与界面配色是相反的，底栏就会在深色界面上顶着一条白玻璃。
 * 这里改为看 MaterialTheme 里真正生效的 surface 亮度，
 * 「跟随系统 / 浅色 / 深色」三种模式 + 莫奈取色都能自动对上。
 */
internal fun ColorScheme.isDarkScheme(): Boolean {
    val luma = 0.2126f * surface.red + 0.7152f * surface.green + 0.0722f * surface.blue
    return luma < 0.5f
}

/** 底栏的一个页签 */
data class GlassTab(
    val label: String,
    val icon: ImageVector
)

/**
 * 液态玻璃底栏。
 *
 * 玻璃本体（模糊 / 折射 / 高光 / 内阴影）由 Apache-2.0 的
 * [Backdrop](https://github.com/Kyant0/AndroidLiquidGlass) 提供；
 * 本文件是我们自己的适配层：负责把 Material 3 动态取色（Monet）喂进去，
 * 并在不支持 RenderEffect 的设备上降级为普通 M3 底栏。
 *
 * 配色取舍：玻璃本体保持中性半透明（浅色偏白 / 深色偏黑并混入一点 surface），
 * 只让**选中态强调色**跟随壁纸取色，这样浅色壁纸下玻璃不会发脏，深色模式下也能真正变深。
 */
@Composable
fun LiquidGlassNavBar(
    backdrop: Backdrop,
    tabs: List<GlassTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** false 时强制使用普通底栏（低端机或用户手动关闭） */
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    // RenderEffect 自 Android 12（API 31）起可用，AGSL RuntimeShader 自 Android 13（API 33）起可用；
    // 更老的设备上效果退化成一块半透明胶囊，可读性差，直接降级为原生底栏。
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        NavigationBar(
            containerColor = colorScheme.surfaceContainer,
            modifier = modifier
        ) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = index == selectedIndex,
                    onClick = { onSelected(index) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.onSecondaryContainer,
                        selectedTextColor = colorScheme.onSurface,
                        indicatorColor = colorScheme.secondaryContainer
                    )
                )
            }
        }
        return
    }

    val isDark = colorScheme.isDarkScheme()

    // 关键：selectedTabIndex 必须是「稳定的 lambda」。
    // 若每次重组都传新 lambda，上游的 remember(selectedTabIndex) 会反复重置内部索引，
    // 胶囊动画就可能漏触发、停在旧位置。这里用 rememberUpdatedState 保持引用稳定。
    val clampedIndex = selectedIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    val currentIndexState = rememberUpdatedState(clampedIndex)
    val selectedTabIndexState = remember(currentIndexState) { { currentIndexState.value } }

    // 同理，onTabSelected 也必须稳定：上游的 DampedDragAnimation 是
    // remember(animationScope) 的，只会捕获首次组合时的 lambda，
    // 若不固定引用，拖拽回调里闭包的就是旧的 currentRoute，守卫判断会失效。
    val onSelectedState = rememberUpdatedState(onSelected)
    val stableOnSelected = remember(onSelectedState) { { index: Int -> onSelectedState.value(index) } }

    LiquidBottomTabs(
        selectedTabIndex = selectedTabIndexState,
        onTabSelected = stableOnSelected,
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier,
        isLightTheme = !isDark,
        accentColor = colorScheme.primary,
        containerColor = if (isDark) {
            // 深色下用「黑 + 一点 surface」的深色玻璃：既压得住背后的内容，
            // 又保留莫奈配色里那点色调，不会是一块死黑的板子
            lerp(Color.Black, colorScheme.surface, 0.35f).copy(alpha = 0.52f)
        } else {
            Color.White.copy(alpha = 0.45f)
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            LiquidBottomTab(onClick = { onSelected(index) }) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    modifier = Modifier.size(24.dp),
                    tint = if (index == selectedIndex) {
                        colorScheme.primary
                    } else {
                        colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == selectedIndex) {
                        colorScheme.primary
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
            }
        }
    }
}
