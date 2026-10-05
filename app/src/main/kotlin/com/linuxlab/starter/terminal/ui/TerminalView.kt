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

import android.graphics.Typeface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linuxlab.starter.terminal.emulator.Cell
import com.linuxlab.starter.terminal.emulator.ColorResolver
import com.linuxlab.starter.terminal.emulator.TerminalBuffer
import com.linuxlab.starter.terminal.emulator.TextAttr
import kotlinx.coroutines.delay
import com.linuxlab.starter.ui.theme.Spacing

/**
 * 终端渲染组件：把 TerminalBuffer 画成带颜色的等宽文本。
 *
 * 性能要点（原生 Canvas 直绘，不走 Compose 文本栈）：
 * 1. 整屏只用 **一个 Canvas**；缓冲区版本号（State）在**绘制阶段**读取，
 *    新输出只触发重绘（draw invalidation），不触发重组；
 * 2. 文本用 android.graphics.Paint 直绘：**同一样式的连续字符合并成 run**，
 *    一段一次 drawText —— 旧实现逐字符建 SpanStyle 的 AnnotatedString，
 *    一次全屏更新要测 30 行 × 80 个 span，是打字卡顿的主源；
 * 3. Paint 按（前景色/粗/斜/下划线）组合缓存，跨帧复用；
 *    背景矩形按 run 的实测宽度绘制，且与默认底色相同的 run 直接跳过；
 * 4. 行列数变化先防抖再同步给 PTY，避免键盘动画期间每帧都触发 TIOCSWINSZ。
 */
@Composable
fun TerminalView(
    buffer: TerminalBuffer,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    backgroundColor: Color = Color(0xFF0C0C0C),
    foregroundColor: Color = Color(0xFFE6E6E6)
) {
    val density = LocalDensity.current
    val textSizePx = with(density) { fontSize.toPx() }.coerceAtLeast(1f)
    val lineHeightPx = with(density) { (fontSize * 1.25f).toPx() }.coerceAtLeast(1f)
    // 文字区内边距：终端字符不再贴屏幕边（8/4 = sm/xs 档）。
    // 背景仍由外层 Box 铺满出血，只有绘制内容整体内缩。
    val padXPx = with(density) { Spacing.sm.toPx() }
    val padYPx = with(density) { Spacing.xs.toPx() }

    // 等宽字体字符宽（只用于估算列数与光标定位）
    val charWidthPx = remember(textSizePx) { measureMonoCharWidth(textSizePx) }
    // 基线在行内的偏移：把字面在 1.25 倍行距里垂直居中
    val baselineOffset = remember(textSizePx, lineHeightPx) {
        val fm = android.graphics.Paint().apply {
            typeface = Typeface.MONOSPACE
            textSize = textSizePx
            fontMetrics
        }.fontMetrics
        (lineHeightPx - fm.descent + fm.ascent) / 2f - fm.ascent
    }

    // 按样式组合缓存的文字 Paint + 共享的背景 Paint，跨帧复用
    val paintCache = remember(textSizePx) { TerminalPaintCache(textSizePx) }
    val defaultFgInt = remember(foregroundColor) { foregroundColor.toArgb() }
    val defaultBgInt = remember(backgroundColor) { backgroundColor.toArgb() }

    var columns by remember { mutableIntStateOf(buffer.columns) }
    // 历史回看偏移：内部状态。拖拽只重组本组件（Canvas 重绘），
    // 不再把整个父屏幕（输入行/快捷键区）卷进重组
    var scrollOffset by remember { mutableIntStateOf(0) }

    BoxWithConstraints(
        modifier = modifier
            .background(backgroundColor)
            .pointerInput(lineHeightPx) {
                detectVerticalDragGestures { _, dragAmount ->
                    val max = buffer.totalLines.coerceAtLeast(0)
                    scrollOffset =
                        (scrollOffset + (dragAmount / lineHeightPx).toInt()).coerceIn(0, max)
                }
            }
    ) {
        val newRows = (constraints.maxHeight / lineHeightPx).toInt().coerceIn(2, 300)
        val newCols = ((constraints.maxWidth - 2 * padXPx) / charWidthPx).toInt().coerceIn(20, 400)

        // 防抖：动画结束后再真正调整缓冲区尺寸并同步给 PTY
        LaunchedEffect(newRows, newCols) {
            delay(120)
            if (newCols != columns) columns = newCols
            buffer.resize(newCols, newRows)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 在绘制阶段读取缓冲区版本号：新输出 → 只重绘，不重组
            @Suppress("UNUSED_VARIABLE")
            val bufferVersion = buffer.version

            val canvas = drawContext.canvas.nativeCanvas
            canvas.save()
            canvas.translate(padXPx, padYPx)
            val visibleRows = ((size.height - 2 * padYPx) / lineHeightPx).toInt().coerceAtLeast(1)
            val total = buffer.totalLines
            val start = (total - visibleRows - scrollOffset).coerceAtLeast(0)
            val rowHeight = lineHeightPx
            val buf = StringBuilder(columns + 8)

            var top = 0f
            for (row in 0 until visibleRows) {
                val lineIndex = start + row
                val cells = buffer.lineForDisplay(lineIndex)
                val cursorColumn =
                    if (lineIndex == buffer.cursorAbsoluteRow) buffer.cursorX else -1
                drawRow(
                    canvas = canvas,
                    cells = cells,
                    width = columns,
                    cursorColumn = cursorColumn,
                    paintCache = paintCache,
                    defaultFgInt = defaultFgInt,
                    defaultBgInt = defaultBgInt,
                    top = top,
                    baseline = top + baselineOffset,
                    rowHeight = rowHeight,
                    charWidth = charWidthPx,
                    buf = buf
                )
                top += rowHeight
            }
            canvas.restore()
        }
    }
}

private fun measureMonoCharWidth(textSizePx: Float): Float {
    val paint = android.graphics.Paint().apply {
        typeface = Typeface.MONOSPACE
        textSize = textSizePx
    }
    return paint.measureText("M").coerceAtLeast(1f)
}

/** 文字 Paint 缓存：key = 前景色 + 粗/斜/下划线 */
private class TerminalPaintCache(private val textSizePx: Float) {
    private val paints = HashMap<Long, android.graphics.Paint>(64)

    fun get(colorArgb: Int, bold: Boolean, italic: Boolean, underline: Boolean): android.graphics.Paint {
        val key = (colorArgb.toLong() and 0xFFFFFFFFL) or
            (if (bold) 1L shl 33 else 0L) or
            (if (italic) 1L shl 34 else 0L) or
            (if (underline) 1L shl 35 else 0L)
        return paints.getOrPut(key) {
            android.graphics.Paint().apply {
                typeface = Typeface.MONOSPACE
                textSize = this@TerminalPaintCache.textSizePx
                isAntiAlias = true
                this.color = colorArgb
                isFakeBoldText = bold
                textSkewX = if (italic) -0.25f else 0f
                isUnderlineText = underline
            }
        }
    }
}

/** 背景 Paint（fill），共享一把 */
private val sharedBgPaint = android.graphics.Paint().apply {
    style = android.graphics.Paint.Style.FILL
}

/**
 * 画一行：把同一样式的连续格合并成 run，一段一次 drawText；
 * 背景矩形按 run 实测宽度绘制，与默认底色相同的 run 跳过不画。
 */
private fun drawRow(
    canvas: android.graphics.Canvas,
    cells: Array<Cell>?,
    width: Int,
    cursorColumn: Int,
    paintCache: TerminalPaintCache,
    defaultFgInt: Int,
    defaultBgInt: Int,
    top: Float,
    baseline: Float,
    rowHeight: Float,
    charWidth: Float,
    buf: StringBuilder
) {
    var runStartCol = 0
    var runFg = 0
    var runBg = 0
    var runBold = false
    var runItalic = false
    var runUnderline = false
    var runHasContent = false

    fun flush(endCol: Int) {
        if (!runHasContent || buf.isEmpty()) return
        val paint = paintCache.get(runFg, runBold, runItalic, runUnderline)
        val text = buf.toString()
        val xStart = runStartCol * charWidth
        val runWidth = paint.measureText(text)
        if (runBg != defaultBgInt) {
            sharedBgPaint.color = runBg
            canvas.drawRect(xStart, top, xStart + runWidth, top + rowHeight, sharedBgPaint)
        }
        canvas.drawText(text, xStart, baseline, paint)
        buf.setLength(0)
        runHasContent = false
    }

    var col = 0
    while (col < width) {
        val cell = cells?.getOrNull(col)
        if (cell != null && (cell.attrs and TextAttr.WIDE_CONT) != 0 && cell.ch == '\u0000') {
            // 双宽字符的右半格：不占字符，跳过
            col++
            continue
        }

        var bold = false
        var italic = false
        var underline = false
        val ch: Char
        var fg: Int
        var bg: Int

        if (cell == null || cell.ch == '\u0000') {
            ch = ' '
            fg = defaultFgInt
            bg = defaultBgInt
        } else {
            ch = cell.ch
            bold = (cell.attrs and TextAttr.BOLD) != 0
            italic = (cell.attrs and TextAttr.ITALIC) != 0
            underline = (cell.attrs and TextAttr.UNDERLINE) != 0
            var fgColor = ColorResolver.resolve(cell.fg, true, bold, defaultFg(defaultFgInt), defaultBg(defaultBgInt))
            var bgColor = ColorResolver.resolve(cell.bg, false, false, defaultFg(defaultFgInt), defaultBg(defaultBgInt))
            if ((cell.attrs and TextAttr.INVERSE) != 0) {
                val t = fgColor; fgColor = bgColor; bgColor = t
            }
            if ((cell.attrs and TextAttr.DIM) != 0) fgColor = ColorResolver.dim(fgColor)
            fg = fgColor.toArgb()
            bg = bgColor.toArgb()
            if (cursorColumn == col) {
                val t = fg; fg = bg; bg = t
            }
        }

        // 与当前 run 样式不同 → 先收尾，再开新 run
        if (runHasContent && (fg != runFg || bg != runBg || bold != runBold ||
                italic != runItalic || underline != runUnderline)
        ) {
            flush(col)
            runStartCol = col
        }
        if (!runHasContent) {
            runStartCol = col
            runFg = fg
            runBg = bg
            runBold = bold
            runItalic = italic
            runUnderline = underline
            runHasContent = true
        }
        buf.append(ch)
        col++
    }
    flush(width)
}

// ColorResolver 接收 Compose Color；这里把缓存的 ARGB int 包装回 Color（值类型，无分配）
private fun defaultFg(argb: Int) = Color(argb)
private fun defaultBg(argb: Int) = Color(argb)
