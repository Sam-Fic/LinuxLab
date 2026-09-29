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

package com.linuxlab.starter.data

import com.linuxlab.starter.model.Command
import com.linuxlab.starter.model.CommandGroup
import com.linuxlab.starter.model.Example
import com.linuxlab.starter.model.Level
import com.linuxlab.starter.model.Param

/**
 * 极简 DSL：让 100+ 条命令的内容写起来像文档一样清爽。
 *
 * 用法：
 * val G = group("file", "文件与目录", "Files & Directories", "folder") {
 *     c("ls", "列出目录内容", "List directory contents", "ls [选项] [路径]") {
 *         p("-l", "长格式显示", "Long format")
 *         e("ls -l", "以长格式列出当前目录", "List in long format")
 *         tip("...")
 *         rel("cd", "pwd")
 *     }
 * }
 */
class CmdBuilder {
    var detailZh = ""
    var detailEn = ""
    internal val params = mutableListOf<Param>()
    internal val examples = mutableListOf<Example>()
    internal val tips = mutableListOf<String>()
    internal val related = mutableListOf<String>()

    /** 参数 / 选项 */
    fun p(flag: String, zh: String, en: String) {
        params += Param(flag, zh, en)
    }

    /** 示例 */
    fun e(code: String, zh: String, en: String = "") {
        examples += Example(code, zh, en)
    }

    /** 小贴士 */
    fun tip(text: String) {
        tips += text
    }

    /** 相关命令 */
    fun rel(vararg names: String) {
        related += names
    }

    /** 更详细的解释（中英对照） */
    fun detail(zh: String, en: String = "") {
        detailZh = zh
        detailEn = en
    }
}

class GroupBuilder {
    internal val items = mutableListOf<Command>()

    fun c(
        name: String,
        zh: String,
        en: String,
        syntax: String = "",
        level: Level = Level.BASIC,
        block: (CmdBuilder.() -> Unit)? = null
    ) {
        val b = CmdBuilder()
        block?.invoke(b)
        items += Command(
            name = name,
            zh = zh,
            en = en,
            syntax = syntax,
            detailZh = b.detailZh,
            detailEn = b.detailEn,
            params = b.params.toList(),
            examples = b.examples.toList(),
            tips = b.tips.toList(),
            related = b.related.toList(),
            level = level
        )
    }
}

fun group(
    id: String,
    zh: String,
    en: String,
    icon: String,
    block: GroupBuilder.() -> Unit
): CommandGroup {
    val g = GroupBuilder()
    g.block()
    return CommandGroup(
        id = id,
        zh = zh,
        en = en,
        icon = icon,
        commands = g.items.map { it.copy(categoryId = id, categoryZh = zh, categoryEn = en) }
    )
}
