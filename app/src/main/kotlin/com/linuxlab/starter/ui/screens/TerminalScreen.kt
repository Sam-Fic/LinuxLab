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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.linuxlab.starter.terminal.PracticeTasks
import com.linuxlab.starter.terminal.TermKind
import com.linuxlab.starter.terminal.TermLine
import com.linuxlab.starter.terminal.TermSignal
import com.linuxlab.starter.terminal.TerminalEngine
import com.linuxlab.starter.ui.components.FilledAssistChip
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing

private val MonoStyle @Composable get() = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    lineHeight = 19.sp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen() {
    val engine = remember { TerminalEngine() }
    val lines = remember { mutableStateListOf<TermLine>().apply { addAll(engine.welcome()) } }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    var input by rememberSaveable { mutableStateOf("") }
    var tasksExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            runCatching { listState.animateScrollToItem(lines.lastIndex) }
        }
    }

    fun runCommand(text: String) {
        val result = engine.run(text)
        if (result.signal == TermSignal.CLEAR) {
            lines.clear()
        }
        lines.addAll(result.lines)
    }

    fun submit() {
        val text = input
        input = ""
        runCommand(text)
    }

    // ---------- 主体布局 ----------
    // 键盘与手势条取并集（各边取较大者）：键盘弹出时手势条被键盘覆盖，
    // 不能 ime + navigationBars 两层相加（会多垫出一个手势条高度）
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
            
    ) {
        TopBar(
            onReset = {
                engine.fs.reset()
                engine.history.clear()
                lines.clear()
                lines.addAll(engine.welcome())
            }
        )

        TasksCard(
            engine = engine,
            expanded = tasksExpanded,
            onToggle = { tasksExpanded = !tasksExpanded },
            onFill = { command ->
                input = command
                runCommand(command)
                input = ""
            }
        )

        // 终端输出区
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = Spacing.lg),
            // 终端输出区：主题 shapes.large 语义化（终端专用容器）
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            ) {
                itemsIndexed(lines) { _, line ->
                    Text(
                        text = if (line.text.isEmpty()) " " else line.text,
                        style = MonoStyle.copy(
                            color = when (line.kind) {
                                TermKind.COMMAND -> MaterialTheme.colorScheme.primary
                                TermKind.ERROR -> MaterialTheme.colorScheme.error
                                TermKind.SUCCESS -> MaterialTheme.colorScheme.tertiary
                                TermKind.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
                                TermKind.OUTPUT -> MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        // 输入行
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            // 终端输入行：全应用统一档——所有容器/行都用 largeIncreased(20dp)
            shape = MaterialTheme.shapes.largeIncreased,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = engine.prompt(),
                    style = MonoStyle.copy(color = MaterialTheme.colorScheme.tertiary),
                    maxLines = 1
                )
                Spacer(Modifier.width(Spacing.sm))
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            when (event.key) {
                                Key.Enter -> { submit(); true }
                                Key.DirectionUp -> { input = engine.previousHistory(input); true }
                                Key.DirectionDown -> { input = engine.nextHistory(input); true }
                                else -> false
                            }
                        },
                    singleLine = true,
                    textStyle = MonoStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (input.isEmpty()) {
                                Text(
                                    text = "输入命令后回车，↑↓ 翻历史",
                                    style = MonoStyle.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    ),
                                    maxLines = 1
                                )
                            }
                            inner()
                        }
                    }
                )
                IconButton(onClick = { submit() }) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.cd_run_command),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 快捷命令
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(quickCommands) { command ->
                FilledAssistChip(
                    onClick = { runCommand(command) },
                    label = {
                        Text(
                            text = command,
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
        Spacer(Modifier.height(Spacing.sm))
    }
}

private val quickCommands = listOf(
    "help",
    "pwd",
    "ls -l",
    "cd notes",
    "cd ..",
    "cat README.md",
    "grep ERROR /var/log/app.log",
    "grep ERROR /var/log/app.log | wc -l",
    "sort notes/todo.txt | uniq -c",
    "tree ~",
    "tasks",
    "clear"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onReset: () -> Unit) {
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
                    Text(stringResource(R.string.title_sandbox_terminal), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "沙盒内的操作不会影响手机",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onReset) {
                Icon(Icons.Filled.RestartAlt, contentDescription = stringResource(R.string.cd_reset_sandbox))
            }
        }
    )
}

@Composable
private fun TasksCard(
    engine: TerminalEngine,
    expanded: Boolean,
    onToggle: () -> Unit,
    onFill: (String) -> Unit
) {
    val finished = PracticeTasks.finished(engine)
    val total = PracticeTasks.all.size

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        // 实战任务卡：M3 Expressive largeIncreased 档（20dp），卡内边距 16dp
        shape = MaterialTheme.shapes.largeIncreased,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "实战任务  $finished/$total",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "照着做一遍，命令就会变成肌肉记忆",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(if (expanded) stringResource(R.string.action_collapse) else stringResource(R.string.action_expand))
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(Spacing.sm))
                PracticeTasks.all.forEach { task ->
                    val done = task.done(engine)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        // 同心圆角：外卡 20.dp（shapes.largeIncreased）与任务块间距 16.dp，圆角差 20-16=4.dp
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                        onClick = { onFill(task.example) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (done) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.padding(start = Spacing.sm).weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = task.example,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "点任意任务可一键执行示例命令",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}
