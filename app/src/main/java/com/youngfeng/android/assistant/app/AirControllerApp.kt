package com.youngfeng.android.assistant.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdSdk
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

        initTTAdSdk()
    }

    private fun initTTAdSdk() {
        // 强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdSdk.init(
            this,
            TTAdConfig.Builder()
                .appId("5352793") // xxxxxxx为穿山甲媒体平台注册的应用ID
                .useTextureView(true) // 默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
                .appName("AirController")
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK) // 落地页主题
                .allowShowNotify(true) // 是否允许sdk展示通知栏提示,若设置为false则会导致通知栏不显示下载进度
                .debug(true) // 测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) // 允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
                .supportMultiProcess(false) // 是否支持多进程，true支持
                .asyncInit(true) // 是否异步初始化sdk,设置为true可以减少SDK初始化耗时。3450版本开始废弃~~
                // .httpStack(new MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
                .build(),
            object : TTAdSdk.InitCallback {
                override fun success() {
                    Log.e("TAG", "初始化成功");
                }

                override fun fail(p0: Int, p1: String?) {
                    Log.e("TAG", "初始化失败：p0: $p0, p1: $p1");
                }
            }

        )
        // 如果明确某个进程不会使用到广告SDK，可以只针对特定进程初始化广告SDK的content
        // if (PROCESS_NAME_XXXX.equals(processName)) {
        //   TTAdSdk.init(context, config);
        // }
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
