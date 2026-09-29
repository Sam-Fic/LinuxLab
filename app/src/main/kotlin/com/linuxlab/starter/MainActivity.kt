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

package com.linuxlab.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.linuxlab.starter.data.UserStore
import com.linuxlab.starter.data.WallpaperStore
import com.linuxlab.starter.ui.AppNav
import com.linuxlab.starter.ui.ThemePrefs
import com.linuxlab.starter.ui.theme.LinuxStarterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { ThemePrefs(context.applicationContext) }
            // 收藏与首页排序也走 SharedPreferences，先在这里初始化好单例
            remember { UserStore.init(context.applicationContext) }
            remember { WallpaperStore.init(context.applicationContext) }

            // Material 3 + 莫奈动态取色：Android 12+ 自动从壁纸取色
            LinuxStarterTheme(
                themeMode = prefs.themeMode,
                useDynamicColor = prefs.useDynamicColor
            ) {
                AppNav(prefs)
            }
        }
    }
}
