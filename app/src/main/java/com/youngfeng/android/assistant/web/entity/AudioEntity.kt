package com.youngfeng.android.assistant.web.entity

data class AudioEntity(
    val id: String,
    val name: String,
    val folder: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val createTime: Long,
    val isMusic: Boolean
)