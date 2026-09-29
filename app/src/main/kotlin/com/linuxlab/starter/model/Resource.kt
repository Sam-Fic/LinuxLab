package com.linuxlab.starter.model

/** 一个外部资源站点的分类 */
data class LinkCategory(
    val id: String,
    val zh: String,
    val en: String,
    val icon: String
)

/** 一条外部资源（文档 / 问答 / 开源项目官网） */
data class LinkItem(
    val name: String,
    val zh: String,
    val url: String,
    val categoryId: String
)
