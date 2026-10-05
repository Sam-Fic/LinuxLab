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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linuxlab.starter.terminal.TerminalSession
import com.linuxlab.starter.terminal.ui.TerminalView
import com.linuxlab.starter.ui.components.FilledAssistChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalToggleButton
import com.linuxlab.starter.ui.components.connectedToggleShapes
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing

// 真实终端渲染专用前景/背景色（终端模拟器不套用 M3 配色，属合理例外）
private val TermBackground = Color(0xFF0B0F0D)
private val TermForeground = Color(0xFFE8F1EC)
// 固定深底上的警示色（取 M3 dark error 80 色调，深浅主题下都可在深底上阅读）
private val TermDanger = Color(0xFFFFB4AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTerminalScreen(
    onFallbackToSandbox: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { TerminalSession(context.applicationContext) }
    val focusRequester = remember { FocusRequester() }

    var fontSize by rememberSaveable { mutableIntStateOf(12) }
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
                            Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text(stringResource(R.string.title_real_terminal), style = MaterialTheme.typography.titleMedium)
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
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.cd_diagnose))
                    }
                    IconButton(onClick = { session.restart(scope) }) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = stringResource(R.string.cd_restart_session))
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
                            fontSize = fontSize.sp,
                            backgroundColor = TermBackground,
                            foregroundColor = TermForeground,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 底部控制区：贴在键盘上方，输入时始终可见。
                    // inset 链：外层 Scaffold 已消费底栏/手势条的避让量，
                    // 这里只需并上 IME（键盘弹出时底栏会隐藏，手势条 insets 由这里接管）
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.ime
                                    .union(WindowInsets.navigationBars)
                                    .only(WindowInsetsSides.Bottom)
                            )
                    ) {
                    // 输入行
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        // 仅上圆角：从官方令牌 shapes.large（16dp）派生，
                        // 把下方两角置 0 —— Material3 内部的 CornerLargeTop 令牌不可用，
                        // 用官方公开的 CornerBasedShape.copy 得到完全等价的结果。
                        shape = MaterialTheme.shapes.large.copy(
                            bottomStart = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp)
                        ),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
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
                                Text(stringResource(R.string.action_enter))
                            }
                        }
                    }

                    if (!imeVisible) {
                    // 快捷命令
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        items(quickCommands) { cmd ->
                            FilledAssistChip(
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
                            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CTRL / ALT：官方 connected button group 多选模式
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                ButtonGroupDefaults.ConnectedSpaceBetween
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalToggleButton(
                                checked = ctrlOn,
                                onCheckedChange = { ctrlOn = it },
                                shapes = connectedToggleShapes(index = 0, count = 2)
                            ) {
                                Text(
                                    text = "CTRL",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                            FilledTonalToggleButton(
                                checked = altOn,
                                onCheckedChange = { altOn = it },
                                shapes = connectedToggleShapes(index = 1, count = 2)
                            ) {
                                Text(
                                    text = "ALT",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
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
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        )
    }
}

@Composable
private fun InstallView(session: TerminalSession) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // M3 Expressive 波浪进度环：解压进度（Expressive 标志性组件）。
        // 终端底恒为深色，用与明暗无关的 fixed 角色保证对比度；
        // 有确定进度的环是唯一指示器，不再叠一个无信息的形变 blob
        CircularWavyProgressIndicator(
            progress = { session.progress.coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.primaryFixedDim,
            trackColor = TermForeground.copy(alpha = 0.2f)
        )
        Text(
            text = session.message ?: "首次启动正在解压内置的 Alpine Linux…",
            style = MaterialTheme.typography.titleMedium,
            color = TermForeground,
            modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.lg)
        )
        Text(
            text = "约 3.8MB，只需一次，之后秒开（全程离线）",
            style = MaterialTheme.typography.bodySmall,
            // 固定深底上的次要文字：不能用主题 onSurfaceVariant（浅色主题下是深灰配深底）
            color = TermForeground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = Spacing.md)
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
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "真实终端启动失败",
            style = MaterialTheme.typography.titleMedium,
            // 固定深底上的警示色：主题 error 在浅色主题下是深红，深底上不可读
            color = TermDanger
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TermForeground,
            modifier = Modifier.padding(top = Spacing.md)
        )
        Text(
            text = "常见原因：设备不是 arm64、系统禁止 ptrace，或设备禁止执行 App 数据目录中的二进制。",
            style = MaterialTheme.typography.bodySmall,
            color = TermForeground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = Spacing.sm)
        )
        if (log.isNotEmpty()) {
            Surface(
                // 终端日志面板：全应用统一档——所有容器/行都用 largeIncreased(20dp)
                shape = MaterialTheme.shapes.largeIncreased,
                color = TermBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg)
            ) {
                Text(
                    text = log.joinToString("\n") { it.ifBlank { " " } },
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = TermForeground,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
        }
        Row(modifier = Modifier.padding(top = Spacing.xl)) {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            Spacer(Modifier.width(Spacing.md))
            TextButton(onClick = onFallback) { Text(stringResource(R.string.action_use_sandbox_terminal)) }
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
