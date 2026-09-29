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

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * 自定义背景：把用户选的图片存进 app 私有目录，全应用作为底层背景。
 *
 * 只做三件事：存图、删图、记住「背景浓度」（图片上盖的那层半透明遮罩，
 * 用来保证文字始终看得清）。读取走系统相册的 SAF，不需要任何存储权限。
 */
object WallpaperStore {

    private const val PREFS = "linux_starter_prefs"
    private const val FILE_NAME = "custom_background.jpg"
    private const val KEY_DIM = "wallpaper_dim"

    /** 图片长边压缩到这个尺寸，避免大图直接进内存 */
    private const val MAX_EDGE = 1280

    private var dir: File? = null
    private var prefs: SharedPreferences? = null

    /** 每次换图 / 删图都会自增，界面靠它重新读图 */
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    /** 背景浓度：0 = 原图，1 = 完全被纯色盖住。默认 0.55，保证文字可读 */
    private val _dim = MutableStateFlow(0.55f)
    val dim: StateFlow<Float> = _dim.asStateFlow()

    fun init(context: Context) {
        if (dir != null) return
        dir = context.filesDir
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _dim.value = prefs?.getFloat(KEY_DIM, 0.55f) ?: 0.55f
        _version.value++
    }

    private fun file(): File? = dir?.let { File(it, FILE_NAME) }

    /** 保存用户选中的图片（会等比压缩后存为 JPEG） */
    fun save(context: Context, uri: Uri): Boolean = runCatching {
        val stream = context.contentResolver.openInputStream(uri) ?: return false
        val raw = BitmapFactory.decodeStream(stream)
        stream.close()
        if (raw == null) return false

        val scale = if (raw.width > MAX_EDGE) MAX_EDGE.toFloat() / raw.width else 1f
        val bmp = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                raw,
                (raw.width * scale).toInt(),
                (raw.height * scale).toInt(),
                true
            )
        } else {
            raw
        }

        val out = file() ?: return false
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        if (bmp !== raw) bmp.recycle()
        raw.recycle()
        _version.value++
        true
    }.getOrDefault(false)

    fun clear() {
        runCatching { file()?.delete() }
        _version.value++
    }

    fun setDim(value: Float) {
        val v = value.coerceIn(0f, 0.92f)
        _dim.value = v
        prefs?.edit { putFloat(KEY_DIM, v) }
    }

    /** 读取当前背景；没设置过返回 null */
    fun load(): Bitmap? {
        val f = file() ?: return null
        if (!f.exists()) return null
        return runCatching { BitmapFactory.decodeFile(f.absolutePath) }.getOrNull()
    }
}
