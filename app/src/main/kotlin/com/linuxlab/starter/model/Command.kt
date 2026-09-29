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

package com.linuxlab.starter.model

/** 难度：入门 / 进阶 */
enum class Level { BASIC, ADVANCED }

/** 一条常用参数（选项） */
data class Param(
    val flag: String,   // 例如 "-l"
    val zh: String,     // 中文说明
    val en: String      // English description
)

/** 一个可复制的示例命令 */
data class Example(
    val code: String,   // 命令行本身（保持英文原文）
    val zh: String,     // 中文说明
    val en: String = "" // English gloss
)

/** 一条命令 / 一个 Shell 语法知识点 */
data class Command(
    val name: String,        // 命令名或语法关键字
    val zh: String,          // 一句话中文说明
    val en: String,          // One-line English description
    val syntax: String = "", // 语法格式
    val detailZh: String = "",
    val detailEn: String = "",
    val params: List<Param> = emptyList(),
    val examples: List<Example> = emptyList(),
    val tips: List<String> = emptyList(),
    val related: List<String> = emptyList(),
    val level: Level = Level.BASIC,
    val categoryId: String = "",
    val categoryZh: String = "",
    val categoryEn: String = "",
    val index: Int = 0       // 在总表中的序号，用于导航
)

/** 一个分类（分组） */
data class CommandGroup(
    val id: String,
    val zh: String,
    val en: String,
    val icon: String,          // 图标键，UI 层映射为 ImageVector
    val commands: List<Command> = emptyList()
)
