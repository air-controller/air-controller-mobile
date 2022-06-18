package com.youngfeng.android.assistant.server.entity

data class FileEntity(
    var name: String,
    var folder: String,
    var size: Long,
    var isDir: Boolean = false,
    var changeDate: Long,
    var isEmpty: Boolean? = false
)
