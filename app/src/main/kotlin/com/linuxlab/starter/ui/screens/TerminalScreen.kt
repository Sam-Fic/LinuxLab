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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Terminal
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
    Column(modifier = Modifier.fillMaxSize()) {
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
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = engine.prompt(),
                    style = MonoStyle.copy(color = MaterialTheme.colorScheme.tertiary),
                    maxLines = 1
                )
                Spacer(Modifier.width(6.dp))
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                        Icons.Outlined.PlayArrow,
                        contentDescription = "执行",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 快捷命令
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickCommands) { command ->
                AssistChip(
                    onClick = { runCommand(command) },
                    label = {
                        Text(
                            text = command,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
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
                    Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("模拟终端 · 实战沙盒", style = MaterialTheme.typography.titleMedium)
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
                Icon(Icons.Outlined.RestartAlt, contentDescription = "重置沙盒")
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Text(if (expanded) "收起" else "展开")
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(6.dp))
                PracticeTasks.all.forEach { task ->
                    val done = task.done(engine)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.06f),
                        onClick = { onFill(task.example) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (done) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (done) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
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
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "点任意任务可一键执行示例命令",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
