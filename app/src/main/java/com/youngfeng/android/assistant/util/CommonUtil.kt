package com.youngfeng.android.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.youngfeng.android.assistant.model.StorageSize
import com.youngfeng.android.assistant.web.entity.ApkInfo
import java.io.File

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

    fun install(context: Context, apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName.toString() + ".provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.fromFile(apkFile),
                "application/vnd.android.package-archive"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun getApkFile(context: Context, packageName: String): File {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return File(applicationInfo.publicSourceDir)
    }

    fun getApkInfo(context: Context, packageName: String): ApkInfo {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val file = File(applicationInfo.publicSourceDir)

        val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()

        return ApkInfo(packageName = packageName, localizeName = appName, file = file)
    }

    fun batchUninstall(context: Context, packages: List<String>) {
    }
}
