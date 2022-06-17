package com.youngfeng.android.assistant.web.util

import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.ext.getString
import com.youngfeng.android.assistant.web.HttpError
import com.youngfeng.android.assistant.web.HttpModule
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity
import java.util.Locale

class ErrorBuilder {
    var mode: HttpModule? = null
    var error: HttpError? = null
    var locale: Locale? = null

    fun locale(locale: Locale): ErrorBuilder {
        this.locale = locale
        return this
    }

    fun module(module: HttpModule): ErrorBuilder {
        this.mode = module
        return this
    }

    fun error(error: HttpError): ErrorBuilder {
        this.error = error
        return this
    }

    fun <T> build(): HttpResponseEntity<T> {
        val code = "${mode?.value}${error?.code}"
        var msg: String? = null

        error?.apply {
            msg = if (null != locale) {
                AirControllerApp.getInstance().getString(locale!!, this.value)
            } else {
                AirControllerApp.getInstance().getString(this.value)
            }
        }
        return HttpResponseEntity(code = code.toIntOrNull() ?: -1, msg = msg, data = null)
    }
}
