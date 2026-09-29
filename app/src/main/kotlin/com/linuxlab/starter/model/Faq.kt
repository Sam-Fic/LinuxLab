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

/** 处理步骤的风险等级：普通 / 需谨慎 / 高危（会丢数据） */
enum class Danger { NONE, CAREFUL, DANGEROUS }

/** 排查步骤：一段中文说明 + 一条可直接复制的命令 */
data class FaqStep(
    val zh: String,
    val command: String = ""
)

/** 一类常见问题 */
data class FaqCategory(
    val id: String,
    val zh: String,
    val en: String,
    val icon: String
)

/** 一条「问题 → 原因 → 处理办法」 */
data class Faq(
    val id: String,
    val categoryId: String,
    val title: String,
    val symptom: String = "",
    val cause: String = "",
    val steps: List<FaqStep> = emptyList(),
    val danger: Danger = Danger.NONE
)
