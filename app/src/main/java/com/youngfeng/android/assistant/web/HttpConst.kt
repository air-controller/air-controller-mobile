package com.youngfeng.android.assistant.web

enum class HttpModule(var value: Int) {
    FileModule(1), ImageModule(2), MusicModule(3)
}

enum class HttpError(var code: String, var value: String) {
    FileIsNotADir("01", "该文件不是一个目录"),
}