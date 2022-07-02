package com.youngfeng.android.assistant.server.entity

import com.youngfeng.android.assistant.server.HttpError
import com.youngfeng.android.assistant.server.HttpModule
import com.youngfeng.android.assistant.server.util.ErrorBuilder

data class HttpResponseEntity<T>(
    var code: Int,
    var data: T?,
    var msg: String?
) {
    companion object {
        const val CODE_SUCCESS = 0

        fun <T> success(data: T? = null): HttpResponseEntity<T> {
            return HttpResponseEntity(CODE_SUCCESS, data, null)
        }

        fun <T> commonError(error: HttpError): HttpResponseEntity<T> {
            return ErrorBuilder().module(HttpModule.SystemModule).error(error).build()
        }
    }
}
