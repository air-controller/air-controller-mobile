package com.youngfeng.android.assistant.web.request

class DeleteFileRequest(
    /**
     * 文件路径
     *
     * 注意：这里可能是文件或文件夹
     */
    val file: String,
    val isDir: Boolean
) : BaseRequest() {
}