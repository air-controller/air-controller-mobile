package com.youngfeng.android.assistant.manager

import android.content.Context
import com.youngfeng.android.assistant.app.AirControllerApp

interface AccessControlManager {
    var allowAccess: Boolean
    var enablePwd: Boolean
    var password: String?

    companion object {
        private val instance = AccessControlManagerImpl()

        @JvmStatic
        fun getInstance() = instance
    }
}

class AccessControlManagerImpl : AccessControlManager {
    companion object {
        const val KEY_ALLOW_ACCESS = "AccessControlManager.allowAccess"
        const val KEY_ENABLE_PWD = "AccessControlManager.enablePwd"
        const val KEY_PASSWD = "AccessControlManager.passwd"
    }

    override var allowAccess: Boolean
        get() {
            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            return pref.getBoolean(KEY_ALLOW_ACCESS, true)
        }
        set(value) {
            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putBoolean(KEY_ALLOW_ACCESS, value)
            editor.apply()
        }

    override var enablePwd: Boolean
        get() {
            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            return pref.getBoolean(KEY_ENABLE_PWD, false)
        }
        set(value) {
            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putBoolean(KEY_ENABLE_PWD, value)
            editor.apply()
        }

    override var password: String?
        get() {
            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            return pref.getString(KEY_PASSWD, null)
        }
        set(value) {
            if (value == null) return

            val pref = AirControllerApp.getInstance()
                .getSharedPreferences("AccessControlManager", Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(KEY_PASSWD, value)
            editor.apply()
        }
}
