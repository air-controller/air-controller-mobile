package com.youngfeng.android.assistant.server.request

data class CreateFileRequest(
    var folder: String,
    var name: String,
    var type: Int
) : BaseRequest() {

    companion object {
        const val TYPE_FILE = 1
        const val TYPE_DIR = 2
    }
}
