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

package com.linuxlab.starter.terminal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.linuxlab.starter.terminal.emulator.Cell
import com.linuxlab.starter.terminal.emulator.ColorResolver
import com.linuxlab.starter.terminal.emulator.TerminalBuffer
import com.linuxlab.starter.terminal.emulator.TextAttr
import kotlinx.coroutines.delay

/**
 * 终端渲染组件：把 TerminalBuffer 画成带颜色的等宽文本。
 *
 * 性能要点：
 * 1. 整个屏只用 **一个 Canvas** 绘制，而不是每行一个 Text 组件，
 *    这样键盘弹出/收起的动画期间（尺寸每帧都在变）不会反复重组几十个组件；
 * 2. 每一行的文本排版（TextLayoutResult）**按需测量并缓存**，
 *    内容没变就不重排，绘制时直接复用；
 * 3. 行列数变化先防抖再同步给 PTY，避免动画期间每帧都触发 TIOCSWINSZ。
 */
@Composable
fun TerminalView(
    buffer: TerminalBuffer,
    scrollOffset: Int,
    onScroll: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    backgroundColor: Color = Color(0xFF0C0C0C),
    foregroundColor: Color = Color(0xFFE6E6E6)
) {
    // 只在内容变化时重建排版缓存（订阅缓冲区版本号）
    val version = buffer.version
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()

    val style = remember(fontSize) {
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            lineHeight = fontSize * 1.25f,
            color = foregroundColor
        )
    }

    val charWidthPx = remember(style, density) {
        (measurer.measure("M".repeat(40), style, maxLines = 1).size.width / 40f).coerceAtLeast(1f)
    }
    val lineHeightPx = remember(fontSize, density) {
        with(density) { (fontSize * 1.25f).toPx() }.coerceAtLeast(1f)
    }

    var columns by remember { mutableIntStateOf(buffer.columns) }

    // 内容或列数变化时才丢掉旧缓存；尺寸变化（键盘动画）不会导致重排
    val cache = remember(version, columns, style, measurer, foregroundColor, backgroundColor) {
        LineLayoutCache(buffer, columns, style, measurer, foregroundColor, backgroundColor)
    }

    BoxWithConstraints(
        modifier = modifier
            .background(backgroundColor)
            .pointerInput(lineHeightPx) {
                detectVerticalDragGestures { _, dragAmount ->
                    onScroll((dragAmount / lineHeightPx).toInt())
                }
            }
    ) {
        val newRows = (constraints.maxHeight / lineHeightPx).toInt().coerceIn(2, 300)
        val newCols = (constraints.maxWidth / charWidthPx).toInt().coerceIn(20, 400)

        // 防抖：动画结束后再真正调整缓冲区尺寸并同步给 PTY
        LaunchedEffect(newRows, newCols) {
            delay(120)
            if (newCols != columns) columns = newCols
            buffer.resize(newCols, newRows)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val visibleRows = (size.height / lineHeightPx).toInt().coerceAtLeast(1)
            val total = cache.lineCount
            val start = (total - visibleRows - scrollOffset).coerceAtLeast(0)
            for (i in 0 until visibleRows) {
                val layout = cache.layout(start + i) ?: continue
                drawText(layout, topLeft = Offset(0f, i * lineHeightPx))
            }
        }
    }
}

/**
 * 行排版缓存：按需测量，测得一次后一直复用，直到内容变化被 remember 丢弃。
 * 回滚行数可能上千，所以绝不能一次性全部测量。
 */
private class LineLayoutCache(
    private val buffer: TerminalBuffer,
    private val columns: Int,
    private val style: TextStyle,
    private val measurer: TextMeasurer,
    private val defaultFg: Color,
    private val defaultBg: Color
) {
    private val cached = HashMap<Int, TextLayoutResult>(96)

    val lineCount: Int get() = buffer.totalLines

    fun layout(index: Int): TextLayoutResult? {
        if (index < 0 || index >= buffer.totalLines) return null
        cached[index]?.let { return it }
        val cells = buffer.lineForDisplay(index) ?: return null
        val cursorColumn = if (index == buffer.cursorAbsoluteRow) buffer.cursorX else -1
        val text = buildLine(cells, columns, cursorColumn, defaultFg, defaultBg)
        val result = measurer.measure(text, style, maxLines = 1, softWrap = false)
        if (cached.size > 2000) cached.clear()
        cached[index] = result
        return result
    }
}

private fun buildLine(
    cells: Array<Cell>?,
    width: Int,
    cursorColumn: Int,
    defaultFg: Color,
    defaultBg: Color
): AnnotatedString = buildAnnotatedString {
    var x = 0
    while (x < width) {
        val cell = cells?.getOrNull(x)
        if (cell == null) {
            append(" ")
            x++
            continue
        }
        val isContinuation = (cell.attrs and TextAttr.WIDE_CONT) != 0
        if (isContinuation && cell.ch == '\u0000') {
            x++
            continue
        }
        val bold = (cell.attrs and TextAttr.BOLD) != 0
        val italic = (cell.attrs and TextAttr.ITALIC) != 0
        val underline = (cell.attrs and TextAttr.UNDERLINE) != 0

        var fg = ColorResolver.resolve(cell.fg, true, bold, defaultFg, defaultBg)
        var bg = ColorResolver.resolve(cell.bg, false, false, defaultFg, defaultBg)
        if ((cell.attrs and TextAttr.INVERSE) != 0) {
            val t = fg
            fg = bg
            bg = t
        }
        if (cursorColumn == x) {
            val t = fg
            fg = bg
            bg = t
        }
        if ((cell.attrs and TextAttr.DIM) != 0) fg = ColorResolver.dim(fg)

        pushStyle(
            SpanStyle(
                color = fg,
                background = bg,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (underline) TextDecoration.Underline else null
            )
        )
        append(cell.ch)
        pop()
        x++
    }
}
