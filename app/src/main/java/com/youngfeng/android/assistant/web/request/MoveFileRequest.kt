package com.youngfeng.android.assistant.web.request

class MoveFileRequest(
    var oldFolder: String,
    var fileName: String,
    var newFolder: String
) : BaseRequest() {
}