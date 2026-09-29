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

/** 目录的用途分类：决定树上的图标与配色，让整棵树「按用途一眼看清」 */
enum class FhsKind {
    /** 系统与程序：/bin /sbin /usr /lib */
    SYSTEM,

    /** 配置：/etc */
    CONFIG,

    /** 会变的数据：/var /tmp */
    DATA,

    /** 用户：/home /root */
    USER,

    /** 设备与内核：/dev /proc /sys */
    DEVICE,

    /** 启动 / 挂载 / 其它：/boot /opt /mnt /media /srv /run */
    MISC
}

/**
 * FHS（文件系统层次结构标准）目录树上的一个节点。
 *
 * @property path 完整路径，如 `/var/log`
 * @property name 目录名，如 `log`
 * @property zh 一句话用途
 * @property detail 详细说明：这里放什么、不放什么
 * @property files 典型文件 / 子目录，每项形如「/etc/passwd —— 用户账号信息」
 * @property commands 与这个目录常打交道的命令，可直接复制
 * @property tips 新手容易踩的坑
 * @property children 子目录
 * @property kind 用途分类
 */
data class FhsNode(
    val path: String,
    val name: String,
    val zh: String,
    val detail: String = "",
    val files: List<String> = emptyList(),
    val commands: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val children: List<FhsNode> = emptyList(),
    /** 用途分类：决定图标与配色 */
    val kind: FhsKind = FhsKind.MISC
)
