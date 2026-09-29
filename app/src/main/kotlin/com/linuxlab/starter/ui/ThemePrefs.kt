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

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.linuxlab.starter.ui.theme.ThemeMode

/** 主题偏好：跟随系统 / 浅色 / 深色，以及莫奈动态取色开关 */
class ThemePrefs(context: Context) {

    private val prefs = context.getSharedPreferences("linux_starter_prefs", Context.MODE_PRIVATE)

    var themeMode: ThemeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_MODE, null) ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var useDynamicColor: Boolean by mutableStateOf(prefs.getBoolean(KEY_DYNAMIC, true))
        private set

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit { putString(KEY_MODE, mode.name) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        useDynamicColor = enabled
        prefs.edit { putBoolean(KEY_DYNAMIC, enabled) }
    }

    private companion object {
        const val KEY_MODE = "theme_mode"
        const val KEY_DYNAMIC = "dynamic_color"
    }
}
