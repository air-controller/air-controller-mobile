package com.youngfeng.android.assistant.web

/**
 * 错误码由模块加具体错误码，注意不同模块的错误码应该从01开始，到19结束，最多20个错误
 *
 * @author Scott Smith 2021/11/2 14:00
 */

enum class HttpModule(var value: Int) {
    FileModule(1), ImageModule(2), MusicModule(3)
}

enum class HttpError(var code: String, var value: String) {
    FileIsNotADir("01", "该文件不是一个目录"),
}