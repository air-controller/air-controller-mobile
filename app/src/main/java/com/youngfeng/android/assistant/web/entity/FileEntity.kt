package com.youngfeng.android.assistant.web.entity

data class FileEntity(
    var name: String,
    var path: String,
    var size: Long,
    var isDir: Boolean = false,
    var isEmpty: Boolean? = false
)