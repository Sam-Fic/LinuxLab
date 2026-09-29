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

package com.linuxlab.starter.terminal.pty

/** 原生 PTY 接口（C 实现见 app/src/main/cpp/pty.c） */
object PtyNative {

    init {
        System.loadLibrary("pty")
    }

    /** 起一个带 PTY 的进程，返回 master fd（<0 表示失败） */
    external fun createPty(
        command: Array<String>,
        environment: Array<String>,
        cwd: String,
        rows: Int,
        cols: Int
    ): Int

    /** 写入（键盘输入），返回写入字节数 */
    external fun writePty(fd: Int, data: ByteArray, length: Int): Int

    /** 读取输出；>0 字节数，0 = EOF，-2 = 暂无数据，-1 = 出错 */
    external fun readPty(fd: Int, buffer: ByteArray): Int

    /** 改变窗口大小并通知进程 */
    external fun resizePty(fd: Int, rows: Int, cols: Int)

    /** 非阻塞 waitpid；-2 = 运行中 */
    external fun waitPty(fd: Int): Int

    /** 结束会话 */
    external fun closePty(fd: Int)
}
