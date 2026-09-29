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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

/** 属性位 */
object TextAttr {
    const val BOLD = 1
    const val DIM = 2
    const val ITALIC = 4
    const val UNDERLINE = 8
    const val BLINK = 16
    const val INVERSE = 32
    const val WIDE_CONT = 64 // 宽字符（中文/emoji）的第二个占位格
}

/** 颜色编码：默认 -1；调色板 0x10000 or index；真彩 0x20000 or RRGGBB */
object TermColor {
    const val DEFAULT = -1
    fun palette(index: Int): Int = 0x10000 or (index and 0xFFFF)
    fun rgb(r: Int, g: Int, b: Int): Int = 0x20000 or ((r shl 16) or (g shl 8) or b)
    fun isDefault(v: Int) = v == DEFAULT
}

class Cell(
    var ch: Char = ' ',
    var fg: Int = TermColor.DEFAULT,
    var bg: Int = TermColor.DEFAULT,
    var attrs: Int = 0
) {
    fun reset(fgColor: Int = TermColor.DEFAULT, bgColor: Int = TermColor.DEFAULT) {
        ch = ' '
        fg = fgColor
        bg = bgColor
        attrs = 0
    }

    fun copyFrom(other: Cell) {
        ch = other.ch
        fg = other.fg
        bg = other.bg
        attrs = other.attrs
    }
}

/**
 * 一个够用的 VT100 / ANSI 终端屏幕缓冲：
 * 支持光标定位、擦除、滚动区域、插入删除、SGR 颜色（16/256/真彩）、
 * 备用屏幕（vi、top 用）、Tab 停位、自动换行、回滚缓冲区。
 */
class TerminalBuffer(
    var columns: Int,
    var rows: Int,
    private val maxScrollback: Int = 3000
) {

    private var screen: Array<Array<Cell>> = allocScreen(rows, columns)
    private var altScreen: Array<Array<Cell>>? = null
    private val scrollback = ArrayDeque<Array<Cell>>()

    var cursorX = 0
        private set
    var cursorY = 0
        private set
    var cursorVisible = true
        private set
    var title: String = "Linux Terminal"
        private set

    var scrollOffset: Int = 0

    /** 用于触发 UI 重绘的版本号 */
    var version by mutableStateOf(0)
        private set

    private var fg = TermColor.DEFAULT
    private var bg = TermColor.DEFAULT
    private var attrs = 0

    private var scrollTop = 0
    private var scrollBottom = rows - 1
    private var autoWrap = true
    private var originMode = false
    private var wrapPending = false
    private var tabStops = BooleanArray(columns) { it % 8 == 0 }

    private var savedCursorX = 0
    private var savedCursorY = 0
    private var savedFg = TermColor.DEFAULT
    private var savedBg = TermColor.DEFAULT
    private var savedAttrs = 0

    // ---------------- 解析状态机 ----------------
    private enum class State { GROUND, ESC, CSI, OSC }

    private var state = State.GROUND
    private val params = StringBuilder()
    private val numericParams = mutableListOf<Int>()
    private var privateMode = false
    private var oscBuffer = StringBuilder()

    private fun allocScreen(r: Int, c: Int) = Array(max(1, r)) { Array(max(1, c)) { Cell() } }
    private fun blankLine(cols: Int = columns) = Array(cols) { Cell() }

    // ---------------- 对外：尺寸 ----------------
    fun resize(newColumns: Int, newRows: Int) {
        if (newColumns == columns && newRows == rows) return
        val old = screen
        val oldCols = columns
        val oldRows = rows
        columns = max(8, newColumns)
        rows = max(2, newRows)
        screen = allocScreen(rows, columns)
        for (y in 0 until min(oldRows, rows)) {
            for (x in 0 until min(oldCols, columns)) {
                screen[y][x].copyFrom(old[y][x])
            }
        }
        altScreen = null
        scrollTop = 0
        scrollBottom = rows - 1
        cursorX = cursorX.coerceIn(0, columns - 1)
        cursorY = cursorY.coerceIn(0, rows - 1)
        tabStops = BooleanArray(columns) { it % 8 == 0 }
        version++
    }

    // ---------------- 对外：取行渲染 ----------------
    val scrollbackSize: Int get() = scrollback.size
    val totalLines: Int get() = scrollback.size + rows
    val cursorAbsoluteRow: Int get() = scrollback.size + cursorY

    /** 取第 index 行（0 .. totalLines-1）用于渲染 */
    fun lineForDisplay(index: Int): Array<Cell>? {
        return when {
            index < 0 -> null
            index < scrollback.size -> scrollback.elementAt(index)
            index < scrollback.size + rows -> screen[index - scrollback.size]
            else -> null
        }
    }

    fun clearScrollback() {
        scrollback.clear()
        scrollOffset = 0
        version++
    }

    // ---------------- 输入 ----------------
    fun feed(text: CharSequence) {
        for (i in text.indices) {
            feedChar(text[i])
        }
        version++
    }

    private fun feedChar(c: Char) {
        when (state) {
            State.GROUND -> when {
                c == '\u001B' -> { state = State.ESC; privateMode = false; params.clear() }
                c == '\r' -> { cursorX = 0; wrapPending = false }
                c == '\n' || c == '\u000B' || c == '\u000C' -> newLine(keepColumn = false)
                c == '\b' -> { cursorX = max(0, cursorX - 1); wrapPending = false }
                c == '\t' -> tabForward()
                c == '\u0007' -> Unit // bell，忽略
                c.code < 0x20 || c == '\u007F' -> Unit // 其它控制字符忽略
                else -> putChar(c)
            }
            State.ESC -> handleEsc(c)
            State.CSI -> handleCsi(c)
            State.OSC -> handleOsc(c)
        }
    }

    private fun handleEsc(c: Char) {
        when (c) {
            '[' -> { state = State.CSI; params.clear(); numericParams.clear(); privateMode = false }
            ']' -> { state = State.OSC; oscBuffer.clear() }
            '(' , ')', '*', '+', '-', '.', '/' -> { /* 字符集选择，忽略后续一个字节 */ state = State.ESC }
            '7' -> saveCursor()
            '8' -> restoreCursor()
            'D' -> newLine(keepColumn = true)
            'E' -> { cursorX = 0; newLine(keepColumn = false) }
            'M' -> reverseIndex()
            'c' -> fullReset()
            '=', '>', '#' -> Unit
            else -> state = State.GROUND
        }
        if (state == State.ESC && c != '[' && c != ']') state = State.GROUND
    }

    private fun handleCsi(c: Char) {
        when {
            c in '0'..'9' -> params.append(c)
            c == ';' -> { pushParam(); }
            c == '?' -> privateMode = true
            c == '>' || c == '<' || c == '=' -> Unit
            c in ' '..'/' -> Unit // 中间字节，忽略
            c in '@'..'~' -> {
                pushParam()
                dispatchCsi(c)
                state = State.GROUND
            }
            else -> state = State.GROUND
        }
    }

    private fun pushParam() {
        val v = params.toString().toIntOrNull()
        numericParams.add(v ?: 0)
        params.clear()
    }

    private fun param(index: Int, default: Int = 1): Int {
        val v = numericParams.getOrNull(index)
        return if (v == null || v == 0) default else v
    }

    private fun handleOsc(c: Char) {
        when (c) {
            '\u0007' -> { finishOsc(); state = State.GROUND }
            '\u001B' -> { finishOsc(); state = State.ESC; params.clear() }
            else -> oscBuffer.append(c)
        }
    }

    private fun finishOsc() {
        val content = oscBuffer.toString()
        val semi = content.indexOf(';')
        val code = if (semi > 0) content.substring(0, semi) else content
        if (code == "0" || code == "1" || code == "2") {
            title = if (semi > 0) content.substring(semi + 1) else ""
        }
    }

    private fun dispatchCsi(final: Char) {
        when (final) {
            'A' -> cursorY = max(scrollTopOrZero(), cursorY - param(0))
            'B' -> cursorY = min(scrollBottomOrLast(), cursorY + param(0))
            'C' -> cursorX = min(columns - 1, cursorX + param(0))
            'D' -> cursorX = max(0, cursorX - param(0))
            'E' -> { cursorX = 0; cursorY = min(scrollBottomOrLast(), cursorY + param(0)) }
            'F' -> { cursorX = 0; cursorY = max(scrollTopOrZero(), cursorY - param(0)) }
            'G', '`' -> cursorX = param(0, 1) - 1
            'd' -> cursorY = param(0, 1) - 1
            'H', 'f' -> {
                val row = param(0, 1) - 1
                val col = param(1, 1) - 1
                cursorY = (if (originMode) scrollTop + row else row).coerceIn(0, rows - 1)
                cursorX = col.coerceIn(0, columns - 1)
                wrapPending = false
            }
            'J' -> eraseInDisplay(param(0, 0))
            'K' -> eraseInLine(param(0, 0))
            'L' -> insertLines(param(0))
            'M' -> deleteLines(param(0))
            'P' -> deleteChars(param(0))
            '@' -> insertChars(param(0))
            'X' -> eraseChars(param(0))
            'S' -> scrollUp(param(0))
            'T' -> scrollDown(param(0))
            'r' -> setScrollRegion()
            's' -> saveCursor()
            'u' -> restoreCursor()
            'h' -> setMode(true)
            'l' -> setMode(false)
            'm' -> setGraphicRendition()
            'n' -> Unit // DSR：本实现不回报
            'c' -> Unit // DA：不回报设备属性
            else -> Unit
        }
        cursorX = cursorX.coerceIn(0, columns - 1)
        cursorY = cursorY.coerceIn(0, rows - 1)
    }

    private fun scrollTopOrZero() = if (originMode) scrollTop else 0
    private fun scrollBottomOrLast() = if (originMode) scrollBottom else rows - 1

    // ---------------- 模式 ----------------
    private fun setMode(enable: Boolean) {
        for (mode in numericParams) {
            if (privateMode) {
                when (mode) {
                    1 -> Unit // 应用光标键
                    7 -> autoWrap = enable
                    25 -> cursorVisible = enable
                    47, 1047, 1049 -> switchScreen(enable)
                    1048 -> if (enable) saveCursor() else restoreCursor()
                    2004 -> Unit // 括号粘贴模式
                }
            } else {
                when (mode) {
                    4 -> Unit
                    20 -> Unit
                }
            }
        }
    }

    private fun switchScreen(toAlternate: Boolean) {
        if (toAlternate) {
            if (altScreen == null) {
                saveCursor()
                altScreen = allocScreen(rows, columns)
                screen = altScreen!!
                clearScreenArea(0, rows - 1)
                cursorX = 0
                cursorY = 0
            }
        } else {
            altScreen?.let {
                screen = allocScreen(rows, columns)
                altScreen = null
                restoreCursor()
            }
        }
    }

    // ---------------- SGR ----------------
    private fun setGraphicRendition() {
        if (numericParams.isEmpty()) numericParams.add(0)
        var i = 0
        while (i < numericParams.size) {
            when (val v = numericParams[i]) {
                0 -> { fg = TermColor.DEFAULT; bg = TermColor.DEFAULT; attrs = 0 }
                1 -> attrs = attrs or TextAttr.BOLD
                2 -> attrs = attrs or TextAttr.DIM
                3 -> attrs = attrs or TextAttr.ITALIC
                4 -> attrs = attrs or TextAttr.UNDERLINE
                5, 6 -> attrs = attrs or TextAttr.BLINK
                7 -> attrs = attrs or TextAttr.INVERSE
                21, 22 -> attrs = attrs and (TextAttr.BOLD or TextAttr.DIM).inv()
                23 -> attrs = attrs and TextAttr.ITALIC.inv()
                24 -> attrs = attrs and TextAttr.UNDERLINE.inv()
                25 -> attrs = attrs and TextAttr.BLINK.inv()
                27 -> attrs = attrs and TextAttr.INVERSE.inv()
                in 30..37 -> fg = TermColor.palette(v - 30)
                38 -> {
                    val (color, skip) = parseExtendedColor(i)
                    if (color != null) fg = color
                    i += skip
                }
                39 -> fg = TermColor.DEFAULT
                in 40..47 -> bg = TermColor.palette(v - 40)
                48 -> {
                    val (color, skip) = parseExtendedColor(i)
                    if (color != null) bg = color
                    i += skip
                }
                49 -> bg = TermColor.DEFAULT
                in 90..97 -> fg = TermColor.palette(v - 90 + 8)
                in 100..107 -> bg = TermColor.palette(v - 100 + 8)
            }
            i++
        }
    }

    /** 返回 (颜色, 额外跳过的参数个数) */
    private fun parseExtendedColor(start: Int): Pair<Int?, Int> {
        val type = numericParams.getOrNull(start + 1) ?: return null to 0
        return when (type) {
            5 -> {
                val index = numericParams.getOrNull(start + 2) ?: return null to 1
                TermColor.palette(index) to 2
            }
            2 -> {
                val r = numericParams.getOrNull(start + 2) ?: return null to 2
                val g = numericParams.getOrNull(start + 3) ?: return null to 2
                val b = numericParams.getOrNull(start + 4) ?: return null to 2
                TermColor.rgb(r, g, b) to 4
            }
            else -> null to 1
        }
    }

    // ---------------- 基本操作 ----------------
    private fun putChar(c: Char) {
        val width = charWidth(c)
        if (autoWrap && wrapPending) {
            cursorX = 0
            newLine(keepColumn = true)
            wrapPending = false
        }
        if (cursorX + width > columns) {
            if (autoWrap) {
                cursorX = 0
                newLine(keepColumn = true)
            } else {
                cursorX = columns - width
            }
        }
        val cell = screen[cursorY][cursorX]
        cell.ch = c
        cell.fg = fg
        cell.bg = bg
        cell.attrs = attrs
        if (width == 2 && cursorX + 1 < columns) {
            val cont = screen[cursorY][cursorX + 1]
            cont.ch = '\u0000'
            cont.fg = fg
            cont.bg = bg
            cont.attrs = attrs or TextAttr.WIDE_CONT
        }
        cursorX += width
        wrapPending = cursorX >= columns
        if (cursorX > columns) cursorX = columns
    }

    private fun newLine(keepColumn: Boolean) {
        if (cursorY == scrollBottom) {
            scrollUp(1)
        } else {
            cursorY = min(rows - 1, cursorY + 1)
        }
        if (!keepColumn) cursorX = 0
        wrapPending = false
    }

    private fun reverseIndex() {
        if (cursorY == scrollTop) scrollDown(1) else cursorY = max(0, cursorY - 1)
        wrapPending = false
    }

    private fun tabForward() {
        var x = cursorX + 1
        while (x < columns - 1 && !tabStops[x]) x++
        cursorX = min(x, columns - 1)
        wrapPending = false
    }

    private fun saveCursor() {
        savedCursorX = cursorX
        savedCursorY = cursorY
        savedFg = fg
        savedBg = bg
        savedAttrs = attrs
    }

    private fun restoreCursor() {
        cursorX = savedCursorX
        cursorY = savedCursorY
        fg = savedFg
        bg = savedBg
        attrs = savedAttrs
    }

    private fun setScrollRegion() {
        val top = param(0, 1) - 1
        val bottom = param(1, rows) - 1
        if (top < bottom && bottom < rows) {
            scrollTop = max(0, top)
            scrollBottom = min(rows - 1, bottom)
            cursorX = 0
            cursorY = if (originMode) scrollTop else 0
        } else {
            scrollTop = 0
            scrollBottom = rows - 1
        }
    }

    private fun scrollUp(n: Int) {
        repeat(n) {
            val removed = screen[scrollTop]
            if (scrollTop == 0 && altScreen == null) pushScrollback(removed)
            for (y in scrollTop until scrollBottom) {
                screen[y] = screen[y + 1]
            }
            screen[scrollBottom] = blankLine()
        }
    }

    private fun scrollDown(n: Int) {
        repeat(n) {
            for (y in scrollBottom downTo scrollTop + 1) {
                screen[y] = screen[y - 1]
            }
            screen[scrollTop] = blankLine()
        }
    }

    private fun pushScrollback(line: Array<Cell>) {
        scrollback.addLast(line)
        while (scrollback.size > maxScrollback) scrollback.removeFirst()
    }

    private fun eraseInDisplay(mode: Int) {
        when (mode) {
            0 -> {
                eraseLineRange(cursorX, columns, cursorY)
                clearScreenArea(cursorY + 1, rows - 1)
            }
            1 -> {
                clearScreenArea(0, cursorY - 1)
                eraseLineRange(0, cursorX + 1, cursorY)
            }
            2, 3 -> {
                clearScreenArea(0, rows - 1)
                if (mode == 3) clearScrollback()
            }
        }
    }

    private fun eraseInLine(mode: Int) {
        when (mode) {
            0 -> eraseLineRange(cursorX, columns, cursorY)
            1 -> eraseLineRange(0, cursorX + 1, cursorY)
            2 -> eraseLineRange(0, columns, cursorY)
        }
    }

    private fun eraseChars(n: Int) {
        eraseLineRange(cursorX, min(columns, cursorX + n), cursorY)
    }

    private fun eraseLineRange(from: Int, to: Int, row: Int) {
        if (row !in 0 until rows) return
        for (x in max(0, from) until min(columns, to)) {
            screen[row][x].reset(fg, bg)
        }
    }

    private fun clearScreenArea(from: Int, to: Int) {
        for (y in max(0, from)..min(rows - 1, to)) {
            screen[y] = blankLine()
        }
    }

    private fun insertLines(n: Int) {
        if (cursorY !in scrollTop..scrollBottom) return
        repeat(n) {
            for (y in scrollBottom downTo cursorY + 1) screen[y] = screen[y - 1]
            screen[cursorY] = blankLine()
        }
    }

    private fun deleteLines(n: Int) {
        if (cursorY !in scrollTop..scrollBottom) return
        repeat(n) {
            for (y in cursorY until scrollBottom) screen[y] = screen[y + 1]
            screen[scrollBottom] = blankLine()
        }
    }

    private fun insertChars(n: Int) {
        val row = screen[cursorY]
        repeat(n) {
            for (x in columns - 1 downTo cursorX + 1) row[x].copyFrom(row[x - 1])
            row[cursorX].reset(fg, bg)
        }
    }

    private fun deleteChars(n: Int) {
        val row = screen[cursorY]
        repeat(n) {
            for (x in cursorX until columns - 1) row[x].copyFrom(row[x + 1])
            row[columns - 1].reset(fg, bg)
        }
    }

    private fun fullReset() {
        screen = allocScreen(rows, columns)
        altScreen = null
        fg = TermColor.DEFAULT
        bg = TermColor.DEFAULT
        attrs = 0
        cursorX = 0
        cursorY = 0
        scrollTop = 0
        scrollBottom = rows - 1
        autoWrap = true
        originMode = false
        wrapPending = false
    }

    companion object {
        /** 东亚宽字符按 2 格处理，保证对齐 */
        fun charWidth(c: Char): Int {
            if (c == '\u0000') return 1
            val cp = c.code
            val wide = cp in 0x1100..0x115F ||
                cp in 0x2E80..0x303E ||
                cp in 0x3041..0x33FF ||
                cp in 0x3400..0x4DBF ||
                cp in 0x4E00..0x9FFF ||
                cp in 0xA000..0xA4CF ||
                cp in 0xAC00..0xD7A3 ||
                cp in 0xF900..0xFAFF ||
                cp in 0xFE10..0xFE19 ||
                cp in 0xFE30..0xFE6F ||
                cp in 0xFF00..0xFF60 ||
                cp in 0xFFE0..0xFFE6 ||
                cp in 0x1F300..0x1F64F ||
                cp in 0x1F900..0x1F9FF ||
                cp in 0x20000..0x3FFFD
            return if (wide) 2 else 1
        }

        fun isWideContinuation(cell: Cell): Boolean = (cell.attrs and TextAttr.WIDE_CONT) != 0
    }
}
