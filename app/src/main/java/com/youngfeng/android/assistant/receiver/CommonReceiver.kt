package com.youngfeng.android.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CommonReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_UPDATE_BATTERY_LEVEL = "com.youngfeng.android.assistant.UPDATE_BATTERY_LEVEL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("@@", "")
    }
}
