package com.youngfeng.android.assistant.server.request

class MoveFileRequest(
    var oldFolder: String,
    var fileName: String,
    var newFolder: String
) : BaseRequest()
