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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linuxlab.starter.terminal.TerminalSession
import com.linuxlab.starter.terminal.ui.TerminalView

// 真实终端渲染专用前景/背景色（终端模拟器不套用 M3 配色，属合理例外）
private val TermBackground = Color(0xFF0B0F0D)
private val TermForeground = Color(0xFFE8F1EC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTerminalScreen(
    bottomBarInset: Dp = 0.dp,
    onFallbackToSandbox: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { TerminalSession(context.applicationContext) }
    val focusRequester = remember { FocusRequester() }

    var fontSize by rememberSaveable { mutableIntStateOf(12) }
    var scrollOffset by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val keyboard = LocalSoftwareKeyboardController.current
    val imeVisible = rememberImeVisible()
    var ctrlOn by rememberSaveable { mutableStateOf(false) }
    var altOn by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { session.start(scope) }
    LaunchedEffect(session.state) {
        if (session.state == TerminalSession.State.RUNNING) focusRequester.requestFocus()
    }
    DisposableEffect(Unit) { onDispose { session.stop() } }

    fun send(bytes: ByteArray) = session.write(bytes)
    fun send(text: String) = session.write(text)

    fun sendTyped(text: String) {
        for (c in text) {
            when {
                ctrlOn -> {
                    send(byteArrayOf(controlCode(c)))
                    ctrlOn = false
                }
                altOn -> {
                    send(byteArrayOf(0x1B))
                    send(c.toString())
                    altOn = false
                }
                else -> send(c.toString())
            }
        }
    }

    Scaffold(
        containerColor = TermBackground,
        contentWindowInsets = WindowInsets.systemBars.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("真实终端 · Alpine Linux", style = MaterialTheme.typography.titleMedium)
                            Text(
                                when (session.state) {
                                    TerminalSession.State.INSTALLING -> "正在准备系统…"
                                    TerminalSession.State.RUNNING ->
                                        if (session.degraded) "兜底模式：仅 shell（无 apk）"
                                        else "proot + PTY 运行中"
                                    TerminalSession.State.EXITED -> "会话已结束"
                                    TerminalSession.State.FAILED -> "启动失败"
                                    else -> "正在启动…"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        fontSize = when (fontSize) { 10 -> 12; 12 -> 14; 14 -> 17; else -> 10 }
                    }) {
                        Text("${fontSize}sp", style = MaterialTheme.typography.labelLarge)
                    }
                    IconButton(onClick = { session.runDiagnostics() }) {
                        Icon(Icons.Outlined.Info, contentDescription = "诊断")
                    }
                    IconButton(onClick = { session.restart(scope) }) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = "重启会话")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TermBackground)
        ) {
            when (session.state) {
                TerminalSession.State.IDLE, TerminalSession.State.INSTALLING -> {
                    InstallView(session)
                }
                TerminalSession.State.FAILED -> {
                    FailedView(
                        message = session.message ?: "未知错误",
                        log = session.startLog,
                        onRetry = { session.restart(scope) },
                        onFallback = onFallbackToSandbox
                    )
                }
                TerminalSession.State.RUNNING, TerminalSession.State.EXITED -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                focusRequester.requestFocus()
                                keyboard?.show()
                            }
                    ) {
                        TerminalView(
                            buffer = session.buffer,
                            scrollOffset = scrollOffset,
                            onScroll = { delta ->
                                val max = (session.buffer.totalLines).coerceAtLeast(0)
                                scrollOffset = (scrollOffset + delta).coerceIn(0, max)
                            },
                            fontSize = fontSize.sp,
                            backgroundColor = TermBackground,
                            foregroundColor = TermForeground,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 底部控制区：贴在键盘上方，输入时始终可见
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                // 键盘高度与导航栏高度取较大者，避免出现黑色空隙；
                                // 玻璃底栏可见时再并上它的高度，避免输入框被底栏压住
                                WindowInsets.ime
                                    .union(WindowInsets.navigationBars)
                                    .union(
                                        WindowInsets(
                                            bottom = with(LocalDensity.current) { bottomBarInset.roundToPx() }
                                        )
                                    )
                                    .only(WindowInsetsSides.Bottom)
                            )
                    ) {
                    // 输入行
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = input,
                                onValueChange = { newValue ->
                                    val old = input.text
                                    val new = newValue.text
                                    if (new.length > old.length) {
                                        sendTyped(new.substring(old.length))
                                    } else if (new.length < old.length) {
                                        repeat(old.length - new.length) { send(byteArrayOf(0x7F)) }
                                    }
                                    input = if (new.contains('\n')) TextFieldValue("") else newValue
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .onKeyEvent { event ->
                                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                                        when (event.key) {
                                            Key.Enter -> { send("\r"); input = TextFieldValue(""); true }
                                            Key.DirectionUp -> { send("\u001B[A"); true }
                                            Key.DirectionDown -> { send("\u001B[B"); true }
                                            Key.DirectionLeft -> { send("\u001B[D"); true }
                                            Key.DirectionRight -> { send("\u001B[C"); true }
                                            Key.Backspace -> { send(byteArrayOf(0x7F)); true }
                                            Key.Tab -> { send("\t"); true }
                                            Key.Escape -> { send("\u001B"); true }
                                            Key.PageUp -> { send("\u001B[5~"); true }
                                            Key.PageDown -> { send("\u001B[6~"); true }
                                            Key.MoveHome -> { send("\u001B[H"); true }
                                            Key.MoveEnd -> { send("\u001B[F"); true }
                                            else -> {
                                                if (event.isCtrlPressed && event.utf16CodePoint > 0) {
                                                    val c = event.utf16CodePoint.toChar().lowercaseChar()
                                                    if (c in 'a'..'z') {
                                                        send(byteArrayOf(controlCode(c)))
                                                        true
                                                    } else false
                                                } else false
                                            }
                                        }
                                    },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    send("\r")
                                    input = TextFieldValue("")
                                }),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (input.text.isEmpty()) {
                                            Text(
                                                text = "点这里输入命令，回车执行",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                            TextButton(onClick = { send("\r"); input = TextFieldValue("") }) {
                                Text("回车")
                            }
                        }
                    }

                    if (!imeVisible) {
                    // 快捷命令
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        items(quickCommands) { cmd ->
                            AssistChip(
                                onClick = { send(cmd + "\r") },
                                label = {
                                    Text(
                                        cmd,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    // 扩展功能键（Termux 风格）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KeyToggle(text = "CTRL", active = ctrlOn) { ctrlOn = !ctrlOn }
                        KeyToggle(text = "ALT", active = altOn) { altOn = !altOn }
                        KeyButton("ESC") { send("\u001B") }
                        KeyButton("TAB") { send("\t") }
                        KeyButton("←") { send("\u001B[D") }
                        KeyButton("↓") { send("\u001B[B") }
                        KeyButton("↑") { send("\u001B[A") }
                        KeyButton("→") { send("\u001B[C") }
                        KeyButton("HOME") { send("\u001B[H") }
                        KeyButton("END") { send("\u001B[F") }
                        KeyButton("PGUP") { send("\u001B[5~") }
                        KeyButton("PGDN") { send("\u001B[6~") }
                        KeyButton("/") { send("/") }
                        KeyButton("-") { send("-") }
                        KeyButton("|") { send("|") }
                        KeyButton("~") { send("~") }
                        KeyButton("^C") { send(byteArrayOf(0x03)) }
                        KeyButton("^D") { send(byteArrayOf(0x04)) }
                        KeyButton("^L") { send(byteArrayOf(0x0C)) }
                        KeyButton("^Z") { send(byteArrayOf(0x1A)) }
                    }
                    }
                    Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

private val quickCommands = listOf(
    "clear",
    "uname -a",
    "ls -la",
    "cat /etc/os-release",
    "ps aux",
    "df -h",
    "free -m",
    "top -n 1",
    "apk update",
    "apk add bash vim python3 git",
    "python3 -c \"print('hello from python')\"",
    "echo \$PATH"
)

@Composable
private fun KeyButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        // 终端虚拟键盘键：主题 small 档位语义化（终端专用部件）
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun KeyToggle(text: String, active: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.small,
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun InstallView(session: TerminalSession) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Terminal,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = session.message ?: "首次启动正在解压内置的 Alpine Linux…",
            style = MaterialTheme.typography.titleMedium,
            color = TermForeground,
            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
        )
        LinearProgressIndicator(
            progress = { session.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "约 3.8MB，只需一次，之后秒开（全程离线）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun FailedView(
    message: String,
    log: List<String>,
    onRetry: () -> Unit,
    onFallback: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "真实终端启动失败",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TermForeground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "常见原因：设备不是 arm64、系统禁止 ptrace，或设备禁止执行 App 数据目录中的二进制。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (log.isNotEmpty()) {
            Surface(
                // 终端日志面板：主题 medium 语义化（终端专用容器）
                shape = MaterialTheme.shapes.medium,
                color = TermBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = log.joinToString("\n") { it.ifBlank { " " } },
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = TermForeground,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Row(modifier = Modifier.padding(top = 20.dp)) {
            TextButton(onClick = onRetry) { Text("重试") }
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onFallback) { Text("改用内置沙盒终端") }
        }
    }
}

private fun controlCode(c: Char): Byte {
    val lower = c.lowercaseChar()
    if (lower in 'a'..'z') return ((lower.code - 'a'.code) + 1).toByte()
    return when (c) {
        '[' -> 0x1B
        '\\' -> 0x1C
        ']' -> 0x1D
        '^' -> 0x1E
        '_' -> 0x1F
        ' ' -> 0x00
        '?' -> 0x7F
        '@' -> 0x00
        else -> 0x00
    }
}

/** 监听软键盘（IME）是否弹出：用于把输入区顶到键盘上方并在打字时收起功能键行 */
@Composable
private fun rememberImeVisible(): Boolean {
    val view = LocalView.current
    var visible by remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val listener = OnApplyWindowInsetsListener { _, insets ->
            visible = insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(view, listener)
        onDispose { ViewCompat.setOnApplyWindowInsetsListener(view, null) }
    }
    return visible
}
