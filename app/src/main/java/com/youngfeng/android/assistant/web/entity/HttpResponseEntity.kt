package com.youngfeng.android.assistant.web.entity

data class HttpResponseEntity<T>(
    var code: Int,
    var data: T?,
    var msg: String?
) {
    companion object {
        const val CODE_SUCCESS = 0

        fun <T> success(data: T): HttpResponseEntity<T> {
            return HttpResponseEntity(CODE_SUCCESS, data, null)
        }
    }
}