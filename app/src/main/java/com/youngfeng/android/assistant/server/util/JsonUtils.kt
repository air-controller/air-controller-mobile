package com.youngfeng.android.assistant.server.util

import com.google.gson.Gson
import java.lang.Exception
import java.lang.reflect.Type

class JsonUtils private constructor() {

    companion object {
        @JvmStatic
        fun <T> parseJson(json: String, type: Type): T? {
            val gson = Gson()
            return try {
                gson.fromJson<T>(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
