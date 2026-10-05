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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.linuxlab.starter.ui.components.CodeBlock
import com.linuxlab.starter.ui.components.SectionTitle
import com.linuxlab.starter.ui.components.rememberCopyAction
import com.linuxlab.starter.ui.components.FilledFilterChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.linuxlab.starter.ui.components.connectedToggleShapes
import androidx.compose.ui.res.stringResource
import com.linuxlab.starter.R
import com.linuxlab.starter.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import com.linuxlab.starter.ui.components.navBarBottomInset

// 9 个权限位在 mask 里的下标：高 3 位属主，中间 3 位属组，低 3 位其他用户
private const val BIT_UR = 8
private const val BIT_UW = 7
private const val BIT_UX = 6
private const val BIT_GR = 5
private const val BIT_GW = 4
private const val BIT_GX = 3
private const val BIT_OR = 2
private const val BIT_OW = 1
private const val BIT_OX = 0

private fun hasBit(mask: Int, index: Int): Boolean = (mask shr index) and 1 == 1
private fun toggleBit(mask: Int, index: Int): Int = mask xor (1 shl index)

/** 把 3 个权限位拼成 rwx 形式的字符串；specialBit 命中时 x 位置显示 s / S / t / T */
private fun triple(value: Int, specialBit: Boolean, specialChar: Char): String {
    val r = if (value and 4 != 0) "r" else "-"
    val w = if (value and 2 != 0) "w" else "-"
    val xPart = when {
        specialBit && value and 1 != 0 -> specialChar.lowercaseChar().toString()
        specialBit -> specialChar.uppercaseChar().toString()
        value and 1 != 0 -> "x"
        else -> "-"
    }
    return "$r$w$xPart"
}

/** 符号写法里的权限字母（用于 chmod u=rwx,g=r-x,o= ） */
private fun letters(value: Int): String =
    (if (value and 4 != 0) "r" else "") +
        (if (value and 2 != 0) "w" else "") +
        (if (value and 1 != 0) "x" else "")

private data class Preset(
    val label: String,
    val mask: Int,
    val special: Int = 0,
    val danger: Boolean = false
)

private val Presets = listOf(
    Preset("644 常规文件", 0b110_100_100),
    Preset("755 脚本 / 目录", 0b111_101_101),
    Preset("600 私钥", 0b110_000_000),
    Preset("700 私有目录", 0b111_000_000),
    Preset("664 组内协作", 0b110_110_100),
    Preset("777 所有人可写（危险）", 0b111_111_111, danger = true),
    Preset("1777 粘滞位 /tmp", 0b111_111_111, special = 1),
    Preset("4755 setuid", 0b111_101_101, special = 4)
)

/**
 * chmod 权限计算器。
 *
 * 勾选「属主 / 属组 / 其他用户」各自的读、写、执行，实时算出八进制数值（755 / 644…）、
 * rwx 符号串，以及可以直接复制运行的 chmod 命令。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChmodScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    // 复制反馈走 M3 官方 Snackbar（Toast 不参与 Material 主题体系）
    val snackbarHostState = remember { SnackbarHostState() }
    val copy = rememberCopyAction(snackbarHostState)

    // 默认 644：最常见的普通文件权限
    var mask by remember { mutableIntStateOf(0b110_100_100) }
    // 特殊位：4 = setuid，2 = setgid，1 = sticky
    var special by remember { mutableIntStateOf(0) }
    var fileName by remember { mutableStateOf("file.txt") }
    var isDir by remember { mutableStateOf(false) }

    val u = (mask shr 6) and 7
    val g = (mask shr 3) and 7
    val o = mask and 7
    val octal = (if (special > 0) "${special}" else "") + "$u$g$o"
    val symbolic = triple(u, special and 4 != 0, 's') +
        triple(g, special and 2 != 0, 's') +
        triple(o, special and 1 != 0, 't')
    val lsPreview = (if (isDir) "d" else "-") + symbolic
    val name = fileName.ifBlank { "文件名" }
    val octalCmd = "chmod $octal $name"
    val symbolCmd = "chmod u=${letters(u)},g=${letters(g)},o=${letters(o)} $name"

    // Expressive 弹性顶栏：随内容滚动收起
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.title_chmod_calculator)) },
                subtitle = { Text(stringResource(R.string.chmod_subtitle)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                ,
            contentPadding = PaddingValues(bottom = navBarBottomInset())
        ) {
            // 结果：官网式大数字 hero 带（主色容器 + display 级数字）
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    // 全应用统一档：全宽 hero 色带与首页大卡同为 extraLargeIncreased(32dp)
                    shape = MaterialTheme.shapes.extraLargeIncreased,
                    color = cs.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 数字切换：Expressive 动效方案的弹簧驱动淡入 + 轻微放大
                        val motion = MaterialTheme.motionScheme
                        AnimatedContent(
                            targetState = octal,
                            transitionSpec = {
                                (fadeIn(animationSpec = motion.fastEffectsSpec()) +
                                    scaleIn(
                                        animationSpec = motion.defaultSpatialSpec(),
                                        initialScale = 0.8f
                                    )) togetherWith
                                    fadeOut(animationSpec = motion.fastEffectsSpec())
                            },
                            label = "octal"
                        ) { value ->
                            Text(
                                text = value,
                                // Expressive 加重字阶：常规 displayLarge 为 Normal 400，
                                // Emphasized 提到 Medium 500，等宽大数字更实、更醒目
                                style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = cs.onPrimaryContainer
                            )
                        }
                        Text(
                            text = symbolic,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = cs.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                        Text(
                            text = "ls -l 里看到的就是：$lsPreview",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onPrimaryContainer.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                }
            }

            // 九宫格勾选
            item { SectionTitle(text = "勾选权限") }
            item {
                Column(Modifier.padding(horizontal = Spacing.lg)) {
                    PermRow(
                        title = "属主 User（u）",
                        subtitle = "文件的拥有者",
                        tint = cs.primary,
                        mask = mask,
                        bits = intArrayOf(BIT_UR, BIT_UW, BIT_UX),
                        onToggle = { mask = toggleBit(mask, it) }
                    )
                    Spacer(Modifier.height(Spacing.md))
                    PermRow(
                        title = "属组 Group（g）",
                        subtitle = "与文件同组的用户",
                        tint = cs.secondary,
                        mask = mask,
                        bits = intArrayOf(BIT_GR, BIT_GW, BIT_GX),
                        onToggle = { mask = toggleBit(mask, it) }
                    )
                    Spacer(Modifier.height(Spacing.md))
                    PermRow(
                        title = "其他用户 Others（o）",
                        subtitle = "既不是属主也不在同组的人",
                        tint = cs.tertiary,
                        mask = mask,
                        bits = intArrayOf(BIT_OR, BIT_OW, BIT_OX),
                        onToggle = { mask = toggleBit(mask, it) }
                    )
                }
            }

            // 生成的命令
            item { SectionTitle(text = "生成的命令") }
            item {
                Column(Modifier.padding(horizontal = Spacing.lg)) {
                    // 「文件 / 目录」是互斥单选，用官方 connected button group：
                    // 首/尾 connected shapes + FilledTonalToggleButton + 单选语义。
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            ButtonGroupDefaults.ConnectedSpaceBetween
                        )
                    ) {
                        FilledTonalToggleButton(
                            checked = !isDir,
                            onCheckedChange = { isDir = false },
                            shapes = connectedToggleShapes(index = 0, count = 2),
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton }
                        ) {
                            Text(stringResource(R.string.chmod_apply_to_file), style = MaterialTheme.typography.labelMedium)
                        }
                        FilledTonalToggleButton(
                            checked = isDir,
                            onCheckedChange = { isDir = true },
                            shapes = connectedToggleShapes(index = 1, count = 2),
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton }
                        ) {
                            Text(stringResource(R.string.chmod_apply_to_dir), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    TextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text(stringResource(R.string.label_file_name)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.sm)
                    )
                    Text(
                        text = "数字写法（最常用）",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.primary,
                        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)
                    )
                    CodeBlock(code = octalCmd, onCopy = copy)
                    Text(
                        text = "符号写法（只改指定的位，不影响其他位）",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.primary,
                        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)
                    )
                    CodeBlock(code = symbolCmd, onCopy = copy)
                    if (isDir) {
                        Text(
                            text = "要连目录里的东西一起改，加 -R：${
                                octalCmd.replace("chmod ", "chmod -R ")
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                }
            }

            // 预设
            item { SectionTitle(text = "常见权限一键套用") }
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Presets.forEach { preset ->
                        FilledFilterChip(
                            selected = mask == preset.mask && special == preset.special,
                            onClick = {
                                mask = preset.mask
                                special = preset.special
                            },
                            label = { Text(preset.label, style = MaterialTheme.typography.labelMedium) },
                            colors = if (preset.danger) {
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cs.errorContainer,
                                    selectedLabelColor = cs.onErrorContainer
                                )
                            } else {
                                FilterChipDefaults.filterChipColors()
                            }
                        )
                    }
                }
            }

            // 特殊位
            item { SectionTitle(text = "进阶：三个特殊位") }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        SpecialBitRow(
                            label = "setuid · 4",
                            desc = "执行时以「文件属主」的身份运行。典型：/usr/bin/passwd（普通用户也能改自己的密码）",
                            checked = special and 4 != 0,
                            onToggle = { special = special xor 4 }
                        )
                        SpecialBitRow(
                            label = "setgid · 2",
                            desc = "执行时以「文件属组」的身份运行；用在目录上时，目录里新建的文件自动继承该组",
                            checked = special and 2 != 0,
                            onToggle = { special = special xor 2 }
                        )
                        SpecialBitRow(
                            label = "粘滞位 sticky · 1",
                            desc = "只有文件所有者和 root 能删。典型：/tmp（权限 drwxrwxrwt）",
                            checked = special and 1 != 0,
                            onToggle = { special = special xor 1 }
                        )
                    }
                }
            }

            // rwx 对文件与目录的不同含义
            item { SectionTitle(text = "r / w / x 到底意味着什么") }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        MeaningHeader()
                        MeaningRow("r 读 4", "读取文件内容（cat、less）", "列出目录里有什么（ls）")
                        MeaningRow("w 写 2", "修改、覆盖文件内容", "在目录里新建 / 删除 / 重命名文件")
                        MeaningRow("x 执行 1", "把它当程序或脚本运行", "进入该目录（cd）、访问里面的文件")
                    }
                }
            }

            // 注意事项
            item { SectionTitle(text = "注意") }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    // 同心圆角：外层 20.dp（shapes.largeIncreased，Expressive 档位）、
                    // 内层代码块距卡边缘 16.dp，圆角差 20-16=4.dp
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = cs.errorContainer
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = cs.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = "别拿 chmod 开玩笑",
                                style = MaterialTheme.typography.labelLarge,
                                color = cs.onErrorContainer
                            )
                        }
                        listOf(
                            "chmod -R 777 / 会把系统权限全部打开，等于把大门拆了，很多服务（含 SSH）会直接拒绝启动",
                            "改别人的文件要 sudo：权限不够的报错是 Permission denied",
                            "目录没有 x 权限时进不去：只有 r 只能看到文件名，cd 会失败",
                            "想看某个文件当前的八进制权限：stat -c '%a %n' 文件名"
                        ).forEachIndexed { index, tip ->
                            Row(Modifier.padding(top = Spacing.sm)) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onErrorContainer
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onErrorContainer,
                                    modifier = Modifier.padding(start = Spacing.sm)
                                )
                            }
                        }
                        CodeBlock(
                            code = "stat -c '%a %n' $name",
                            modifier = Modifier.padding(top = Spacing.md),
                            onCopy = copy,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermRow(
    title: String,
    subtitle: String,
    tint: Color,
    mask: Int,
    bits: IntArray,
    onToggle: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        // 权限位行卡：M3 Expressive largeIncreased 档（20dp），卡内边距 16dp
        shape = MaterialTheme.shapes.largeIncreased,
        color = cs.surfaceContainerHigh
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
            ) {
                // 官网式 connected button group：一组 r/w/x 是三个多选
                // FilledTonalToggleButton，用首/中/尾 connected shapes 连成一组。
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    val permColors = FilledTonalToggleButtonDefaults.colors(
                        checkedContainerColor = tint,
                        checkedContentColor = cs.surface
                    )
                    FilledTonalToggleButton(
                        checked = hasBit(mask, bits[0]),
                        onCheckedChange = { onToggle(bits[0]) },
                        shapes = connectedToggleShapes(index = 0, count = 3),
                        colors = permColors,
                        modifier = Modifier.weight(1f)
                    ) {
                        PermSegmentLabel("读 r·4")
                    }
                    FilledTonalToggleButton(
                        checked = hasBit(mask, bits[1]),
                        onCheckedChange = { onToggle(bits[1]) },
                        shapes = connectedToggleShapes(index = 1, count = 3),
                        colors = permColors,
                        modifier = Modifier.weight(1f)
                    ) {
                        PermSegmentLabel("写 w·2")
                    }
                    FilledTonalToggleButton(
                        checked = hasBit(mask, bits[2]),
                        onCheckedChange = { onToggle(bits[2]) },
                        shapes = connectedToggleShapes(index = 2, count = 3),
                        colors = permColors,
                        modifier = Modifier.weight(1f)
                    ) {
                        PermSegmentLabel("执行 x·1")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermSegmentLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = FontFamily.Monospace
        ),
        maxLines = 1
    )
}

@Composable
private fun SpecialBitRow(
    label: String,
    desc: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        FilledFilterChip(
            selected = checked,
            onClick = onToggle,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) }
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier
                .padding(start = Spacing.sm)
                .weight(1f)
        )
    }
}

@Composable
private fun MeaningHeader() {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(bottom = Spacing.xs)) {
        Text(
            text = "权限",
            style = MaterialTheme.typography.labelLarge,
            color = cs.primary,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = "对文件",
            style = MaterialTheme.typography.labelLarge,
            color = cs.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "对目录",
            style = MaterialTheme.typography.labelLarge,
            color = cs.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MeaningRow(perm: String, forFile: String, forDir: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = perm,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = cs.onSurface,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = forFile,
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = forDir,
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
