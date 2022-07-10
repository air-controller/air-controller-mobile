package com.youngfeng.android.assistant.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.youngfeng.android.assistant.BuildConfig
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import timber.log.Timber
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AirControllerApp : Application() {
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mExecutorService by lazy {
        val threadPoolExecutor = ThreadPoolExecutor(
            1, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue<Runnable>()
        )
        threadPoolExecutor.allowCoreThreadTimeOut(false)
        threadPoolExecutor
    }

    companion object {
        private lateinit var INSTANCE: AirControllerApp

        @JvmStatic
        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()

        clearExpiredZipFiles()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun clearExpiredZipFiles() {
        mExecutorService.submit {
            val zipFileRecordDao = RoomDatabaseHolder.getRoomDatabase(this).zipFileRecordDao()
            val zipFiles = zipFileRecordDao.findAll()

            zipFiles.forEach {
                val now = System.currentTimeMillis()
                // 超过指定时间的zip临时文件移除掉
                if (now - it.createTime > Constants.KEEP_TEMP_ZIP_FILE_DURATION) {
                    val zipFile = File(it.path)
                    if (zipFile.exists()) {
                        if (zipFile.delete()) {
                            zipFileRecordDao.delete(it)
                        }
                    }
                }
            }
        }
    }

    fun runOnUiThread(action: () -> Unit) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            action.invoke()
        } else {
            mHandler.post(action)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        mExecutorService.shutdownNow()
    }
}
