package com.youngfeng.android.assistant.web.entity

data class AlbumEntity(
    var id: String,
    var name: String,
    var photoNum: Long = 0,
    var path: String,
    var coverImageId: String
)
