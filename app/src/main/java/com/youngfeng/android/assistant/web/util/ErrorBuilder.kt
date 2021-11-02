package com.youngfeng.android.assistant.web.util

import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity

class ErrorBuilder {
    var mode: HttpModule? = null
    var error: HttpError? = null

    fun mode(module: HttpModule): ErrorBuilder {
        this.mode = module
        return this
    }

    fun error(error: HttpError): ErrorBuilder {
        this.error = error
        return this
    }

    fun <T> build(): HttpResponseEntity<T> {
        val code = "${mode?.value}${error?.code}"
        return HttpResponseEntity(code = code.toIntOrNull() ?: -1, msg = error?.value, data = null)
    }
}