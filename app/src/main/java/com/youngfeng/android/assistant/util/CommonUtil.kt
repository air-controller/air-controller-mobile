package com.youngfeng.android.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import com.youngfeng.android.assistant.model.StorageSize

object CommonUtil {

    fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getExternalStorageSize(): StorageSize {
        val dir = Environment.getExternalStorageDirectory()
        return StorageSize(dir.totalSpace, dir.freeSpace)
    }

    fun openExternalBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }
}
