package com.youngfeng.android.assistant.ext

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun Context.getString(locale: Locale, resId: Int): String {
    val config = Configuration(this.resources.configuration)
    config.setLocale(locale)

    return createConfigurationContext(config).getText(resId).toString()
}
