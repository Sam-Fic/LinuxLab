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

/** 学习路线上的一个阶段；groupIds 指向 [Repository.groupById] 里的分类 */
data class StudyStep(
    val title: String,
    val desc: String,
    val groupIds: List<String>
)

/**
 * 给初学者的建议学习顺序。
 *
 * 顺序的原则是「先能活下去，再谈效率」：先学会在文件系统里走路和看内容，
 * 遇到 Permission denied 能自己解决，然后才是装软件、查网络，最后用 Shell 脚本把重复劳动自动化。
 */
val StudyPath: List<StudyStep> = listOf(
    StudyStep(
        title = "第 1 步 · 在文件系统里走路",
        desc = "先认路：知道自己在哪（pwd）、去哪（cd）、这里有什么（ls）、怎么新建和移动（mkdir/cp/mv/rm）。" +
            "这一组不会，后面所有命令都无从下手。",
        groupIds = listOf("file")
    ),
    StudyStep(
        title = "第 2 步 · 看懂文件里写了什么",
        desc = "Linux 下配置、日志、报错全是纯文本。学会 cat / less / head / tail / grep，" +
            "你就能自己读日志、找报错行，而不是只会截图问人。",
        groupIds = listOf("text", "search")
    ),
    StudyStep(
        title = "第 3 步 · 搞定 Permission denied",
        desc = "新手最常卡住的地方。搞懂 rwx 三种权限、属主属组、sudo 什么时候该用，" +
            "配合首页的「权限计算器」练几次就通了。",
        groupIds = listOf("perm")
    ),
    StudyStep(
        title = "第 4 步 · 看程序在不在、服务起没起",
        desc = "ps / top / kill 管进程，systemctl 管开机自启的服务。" +
            "「网站打不开」这类问题，一半是进程没在跑、一半是服务崩了。",
        groupIds = listOf("process", "service")
    ),
    StudyStep(
        title = "第 5 步 · 装软件、连网络",
        desc = "apt / dnf / pacman 装包，ip / ss / curl / ssh 查网络与远程登录。" +
            "到这一步你已经能独立把一台机器配起来。",
        groupIds = listOf("package", "net")
    ),
    StudyStep(
        title = "第 6 步 · 看清这台机器的状态",
        desc = "磁盘满了、内存爆了、CPU 打满，都靠这一组命令定位：" +
            "df / du / free / uname / env。配合首页的「FHS 目录结构图解」知道东西都在哪。",
        groupIds = listOf("disk", "system")
    ),
    StudyStep(
        title = "第 7 步 · 打包、备份与自动化",
        desc = "tar 打包压缩，然后学 Shell 脚本把每天重复的操作写成一行命令。" +
            "这是从「会用 Linux」到「用 Linux 提高效率」的分水岭。",
        groupIds = listOf("archive", "shell")
    )
)
