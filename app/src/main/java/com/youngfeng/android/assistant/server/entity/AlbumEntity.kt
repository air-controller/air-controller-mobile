package com.youngfeng.android.assistant.server.entity

data class AlbumEntity(
    var id: String,
    var name: String,
    var photoNum: Long = 0,
    var path: String,
    var coverImageId: String
)
