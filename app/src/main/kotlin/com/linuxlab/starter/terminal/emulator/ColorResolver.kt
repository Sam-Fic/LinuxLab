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

package com.linuxlab.starter.terminal.emulator

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/** xterm 调色板 + 24 位真彩 → Compose Color */
object ColorResolver {

    private val basic = intArrayOf(
        0x000000, 0xE06C75, 0x98C379, 0xE5C07B, 0x61AFEF, 0xC678DD, 0x56B6C2, 0xABB2BF,
        0x5C6370, 0xFF6B7A, 0xB5E890, 0xF2D98C, 0x74B7F0, 0xD98BE0, 0x6FD3DE, 0xFFFFFF
    )

    fun color256(index: Int): Int {
        if (index < 0) return basic[0]
        if (index < 16) return basic[index]
        if (index < 232) {
            val i = index - 16
            val r = levels[(i / 36) % 6]
            val g = levels[(i / 6) % 6]
            val b = levels[i % 6]
            return (r shl 16) or (g shl 8) or b
        }
        val v = 8 + (index - 232) * 10
        return (v shl 16) or (v shl 8) or v
    }

    private val levels = intArrayOf(0, 95, 135, 175, 215, 255)

    /**
     * @param value TermColor 编码值
     * @param bold 前景色加粗时，0-7 号色自动提亮为 8-15
     */
    fun resolve(
        value: Int,
        isForeground: Boolean,
        bold: Boolean,
        defaultFg: Color,
        defaultBg: Color
    ): Color {
        if (value == TermColor.DEFAULT) return if (isForeground) defaultFg else defaultBg
        val rgb: Int = when {
            (value and 0x20000) != 0 -> value and 0xFFFFFF
            (value and 0x10000) != 0 -> {
                var idx = value and 0xFFFF
                if (isForeground && bold && idx < 8) idx += 8
                color256(idx)
            }
            else -> value and 0xFFFFFF
        }
        return Color(0xFF000000.toInt() or rgb)
    }

    /** 用于反显 / 光标 */
    fun dim(color: Color, factor: Float = 0.6f): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private fun Int.roundToByte(): Int = this.coerceIn(0, 255)

    fun luminance(rgb: Int): Float {
        val r = ((rgb shr 16) and 0xFF) / 255f
        val g = ((rgb shr 8) and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        return ((0.299f * r + 0.587f * g + 0.114f * b) * 100).roundToInt() / 100f
    }
}
