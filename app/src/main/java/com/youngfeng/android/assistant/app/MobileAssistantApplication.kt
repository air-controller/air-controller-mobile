package com.youngfeng.android.assistant.app

import android.app.Application

class MobileAssistantApplication : Application() {

    companion object {
        private lateinit var INSTANCE: MobileAssistantApplication

        @JvmStatic
        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
    }
}
