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
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 首页可以拖动排序的板块 */
enum class HomeSection(val id: String, val title: String) {
    DAILY("daily", "今日推荐命令"),
    DESKTOP("desktop", "桌面环境教程"),
    TUTORIAL("tutorial", "终端命令教程"),
    FAVORITES("favorites", "我的收藏"),
    CHMOD("chmod", "权限计算器"),
    FHS("fhs", "FHS 目录结构图解")
}

/**
 * 用户数据：命令收藏、首页板块顺序。
 *
 * 两者都存在同一个 SharedPreferences（和主题设置一个文件），
 * 用 StateFlow 暴露，界面 collectAsState 即可自动刷新。
 */
object UserStore {

    private const val PREFS = "linux_starter_prefs"
    private const val KEY_FAVORITES = "favorite_ids"
    private const val KEY_ORDER = "home_section_order"
    private const val KEY_ONBOARDING = "onboarding_done"

    private var prefs: SharedPreferences? = null

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    private val _sectionOrder = MutableStateFlow<List<String>>(HomeSection.entries.map { it.id })
    val sectionOrder: StateFlow<List<String>> = _sectionOrder.asStateFlow()

    /**
     * 是否已经看过首次引导。默认 true，等 init() 读到真实值后再决定要不要跳引导页，
     * 这样 AppNav 首帧不会因为「还没读偏好」而误跳一次。
     */
    private val _onboardingDone = MutableStateFlow(true)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val p = prefs ?: return
        _favorites.value = p.getStringSet(KEY_FAVORITES, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        _sectionOrder.value = parseOrder(p.getString(KEY_ORDER, null))
        _onboardingDone.value = p.getBoolean(KEY_ONBOARDING, false)
    }

    /** 收藏 / 取消收藏一条命令（按命令的唯一编号） */
    fun toggleFavorite(index: Int) {
        val next = if (index in _favorites.value) _favorites.value - index else _favorites.value + index
        _favorites.value = next
        prefs?.edit { putStringSet(KEY_FAVORITES, next.map { it.toString() }.toSet()) }
    }

    fun isFavorite(index: Int): Boolean = index in _favorites.value

    /** 引导看完：写入偏好，之后启动直接进入首页 */
    fun setOnboardingDone() {
        _onboardingDone.value = true
        prefs?.edit { putBoolean(KEY_ONBOARDING, true) }
    }

    /** 重新观看引导（设置页入口）：置回 false，AppNav 会自动跳到引导页 */
    fun replayOnboarding() {
        _onboardingDone.value = false
        prefs?.edit { putBoolean(KEY_ONBOARDING, false) }
    }

    /** 保存首页板块的新顺序（拖动过程中每次交换都会调用） */
    fun setSectionOrder(ids: List<String>) {
        _sectionOrder.value = ids
        prefs?.edit { putString(KEY_ORDER, ids.joinToString(",")) }
    }

    /**
     * 解析保存下来的顺序：
     * 只认得已经存在的板块 id，重复的去掉，新版新增的板块补到末尾。
     */
    private fun parseOrder(raw: String?): List<String> {
        val all = HomeSection.entries.map { it.id }
        if (raw.isNullOrBlank()) return all
        val saved = raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .filter { it in all }
        return saved + all.filter { it !in saved }
    }
}
