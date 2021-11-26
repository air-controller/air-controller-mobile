package com.youngfeng.android.assistant.web.entity

data class VideoEntity(
    var id: Long,
    var name: String,
    var path: String,
    var duration: Long,
    var size: Long,
    var createTime: Long,
    var lastModifyTime: Long
)
