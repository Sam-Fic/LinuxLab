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
import com.linuxlab.starter.model.Faq
import com.linuxlab.starter.model.FaqCategory
import com.linuxlab.starter.model.FhsNode
import com.linuxlab.starter.model.DesktopEnv
import com.linuxlab.starter.model.LinkCategory
import com.linuxlab.starter.model.LinkItem
import com.linuxlab.starter.model.Level
import java.util.Calendar

/** 命令数据仓库：汇总所有分类，提供检索与推荐 */
object Repository {

    private val rawGroups: List<CommandGroup> = listOf(
        FileDirGroup,
        TextViewGroup,
        SearchGroup,
        PermUserGroup,
        ProcessGroup,
        SystemInfoGroup,
        PackageGroup,
        NetworkGroup,
        DiskGroup,
        ArchiveGroup,
        ServiceGroup,
        ShellGroup
    )

    private var cursor = 0

    /** 全部分类（顺序即首页展示顺序），条目已带全局序号 */
    val groups: List<CommandGroup> = rawGroups.map { group ->
        group.copy(commands = group.commands.map { it.copy(index = cursor++) })
    }

    /** 全部条目（带全局序号，用于导航） */
    val all: List<Command> = groups.flatMap { it.commands }

    private val nameIndex: Map<String, Command> = all.associateBy { it.name }

    val totalCount: Int get() = all.size
    val categoryCount: Int get() = groups.size
    val basicCount: Int get() = all.count { it.level == Level.BASIC }

    fun groupById(id: String): CommandGroup? = groups.firstOrNull { it.id == id }

    fun byName(name: String): Command? = nameIndex[name]

    // ---------------- 常见问题排查 ----------------
    /** 问题分类 */
    val faqCategories: List<FaqCategory> = FaqCategories

    /** 全部问题条目 */
    val faqs: List<Faq> = Faqs

    val faqCount: Int get() = Faqs.size

    fun faqCategoryZh(id: String): String =
        FaqCategories.firstOrNull { it.id == id }?.zh ?: ""

    // ---------------- 桌面环境教程 ----------------
    /** 桌面环境的分层结构说明 */
    val desktopLayers = DesktopLayers

    /** 全部桌面环境教程 */
    val desktops: List<DesktopEnv> = DesktopEnvs

    val desktopCount: Int get() = DesktopEnvs.size

    fun desktopById(id: String): DesktopEnv? = DesktopEnvs.firstOrNull { it.id == id }

    // ---------------- FHS 目录结构 ----------------
    /** FHS 目录树（一级目录 + 常用二级目录） */
    val fhsTree: List<FhsNode> = FhsTree

    /** 树上的节点总数（含子目录） */
    val fhsCount: Int = countNodes(FhsTree)

    private fun countNodes(nodes: List<FhsNode>): Int =
        nodes.sumOf { 1 + countNodes(it.children) }

    // ---------------- 资源导航 ----------------
    /** 资源分类 */
    val linkCategories: List<LinkCategory> = LinkCategories

    /** 全部外部资源 */
    val links: List<LinkItem> = Links

    val linkCount: Int get() = Links.size

    fun linkCategoryZh(id: String): String =
        LinkCategories.firstOrNull { it.id == id }?.zh ?: ""

    /** 资源检索：名称、说明、网址参与匹配 */
    fun linkSearch(query: String): List<LinkItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return Links.mapNotNull { link ->
            var s = 0
            if (link.name.lowercase().contains(q)) s += 100
            if (link.zh.lowercase().contains(q)) s += 50
            if (link.url.lowercase().contains(q)) s += 40
            if (s > 0) link to s else null
        }.sortedByDescending { it.second }.map { it.first }
    }

    /** 问题检索：标题、报错现象、原因、处理命令全部参与匹配 */
    fun faqSearch(query: String): List<Faq> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return Faqs.mapNotNull { faq ->
            var s = 0
            if (faq.title.lowercase().contains(q)) s += 100
            if (faq.symptom.lowercase().contains(q)) s += 60
            if (faq.cause.lowercase().contains(q)) s += 30
            if (faq.steps.any { it.command.lowercase().contains(q) || it.zh.lowercase().contains(q) }) s += 20
            if (s > 0) faq to s else null
        }.sortedByDescending { it.second }.map { it.first }
    }

    fun byIndex(index: Int): Command? = all.getOrNull(index)

    /** 每日一命令：按一年中的第几天轮换 */
    fun dailyCommand(): Command {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[day % all.size]
    }

    /**
     * 今日推荐命令组：首页翻卡用。第一条与 [dailyCommand] 一致，
     * 后续在全库均匀取样，整组每天随 dayOfYear 轮换。
     */
    fun dailyCommands(count: Int = 6): List<Command> {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val stride = (all.size / count.coerceAtLeast(1)).coerceAtLeast(1)
        return List(count) { i -> all[(day + i * stride) % all.size].let(::requireNotNull) }
    }

    /**
     * 全局搜索：命令名、中英文说明、语法、参数、示例全部参与匹配，
     * 并按匹配位置打分排序（名字命中排最前）。
     */
    fun search(query: String): List<Command> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        return all.mapNotNull { cmd ->
            val score = score(cmd, q)
            if (score > 0) cmd to score else null
        }
            .sortedWith(compareByDescending<Pair<Command, Int>> { it.second }
                .thenBy { it.first.level }
                .thenBy { it.first.name })
            .map { it.first }
    }

    private fun score(cmd: Command, q: String): Int {
        val name = cmd.name.lowercase()
        var s = 0
        if (name == q) s += 1000
        else if (name.startsWith(q)) s += 400
        else if (name.contains(q)) s += 200

        if (cmd.zh.contains(q, ignoreCase = true)) s += 90
        if (cmd.en.lowercase().contains(q)) s += 80
        if (cmd.categoryZh.contains(q, ignoreCase = true) || cmd.categoryEn.lowercase().contains(q)) s += 60
        if (cmd.syntax.lowercase().contains(q)) s += 40
        if (cmd.detailZh.contains(q, ignoreCase = true) || cmd.detailEn.lowercase().contains(q)) s += 30
        if (cmd.params.any { it.flag.lowercase().contains(q) || it.zh.contains(q, ignoreCase = true) }) s += 25
        if (cmd.examples.any { it.code.lowercase().contains(q) || it.zh.contains(q, ignoreCase = true) }) s += 20
        if (cmd.tips.any { it.contains(q, ignoreCase = true) }) s += 10

        // 入门命令优先展示
        if (s > 0 && cmd.level == Level.BASIC) s += 5
        return s
    }
}
