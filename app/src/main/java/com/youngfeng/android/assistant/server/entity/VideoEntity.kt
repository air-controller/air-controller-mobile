package com.youngfeng.android.assistant.server.entity

data class VideoEntity(
    var id: Long,
    var name: String,
    var path: String,
    var duration: Long,
    var size: Long,
    var createTime: Long,
    var lastModifyTime: Long
)
