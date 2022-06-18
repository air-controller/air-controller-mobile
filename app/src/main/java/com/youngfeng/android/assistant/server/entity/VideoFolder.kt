package com.youngfeng.android.assistant.server.entity

data class VideoFolder(
    var id: String,
    var name: String,
    var videoCount: Int = 0,
    var coverVideoId: Long,
    var path: String
)
