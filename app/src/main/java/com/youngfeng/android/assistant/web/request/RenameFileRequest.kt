package com.youngfeng.android.assistant.web.request

data class RenameFileRequest(
    var folder: String,
    var file: String,
    var newName: String,
    var isDir: Boolean
) : BaseRequest()
