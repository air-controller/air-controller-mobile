package com.youngfeng.android.assistant.util

import android.os.Environment
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import timber.log.Timber
import java.io.File

object PathHelper {

    fun uploadFileDir(): File {
        val uploadDir = File(rootFileDir(), "upload")

        if (!uploadDir.exists()) {
            try {
                if (uploadDir.mkdirs()) {
                    return uploadDir
                }
            } catch (e: Exception) {
                Timber.e("Create upload dir failure, reason: ${e.message}")
            }
        } else {
            return uploadDir
        }

        return backFileDir()
    }

    fun zipFileDir(): File {
        val zipDir = File(rootFileDir(), "zip")

        if (!zipDir.exists()) {
            try {
                if (zipDir.mkdirs()) {
                    return zipDir
                }
            } catch (e: Exception) {
                Timber.e("Create upload dir failure, reason: ${e.message}")
            }
        } else {
            return zipDir
        }

        return backFileDir()
    }

    private fun rootFileDir(): File {
        val context = MobileAssistantApplication.getInstance()
        val externalDir = context.getExternalFilesDir(null)

        if (Environment.getExternalStorageState(externalDir) == Environment.MEDIA_MOUNTED) {
            if (null != externalDir) return externalDir
        }

        return context.filesDir
    }

    private fun backFileDir() = MobileAssistantApplication.getInstance().filesDir
}
