package com.youngfeng.android.assistant.server.request

data class RenameFileRequest(
    var folder: String,
    var file: String,
    var newName: String,
    var isDir: Boolean
) : BaseRequest()
