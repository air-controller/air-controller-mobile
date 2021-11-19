package com.youngfeng.android.assistant.web.entity

data class ImageEntity(
    var id: String,
    var mimeType: String,
    var thumbnail: String?,
    var path: String,
    var width: Int? = null,
    var height: Int? = null,
    var modifyTime: Long? = null,
    var createTime: Long? = null,
    var displayName: String? = null
)
