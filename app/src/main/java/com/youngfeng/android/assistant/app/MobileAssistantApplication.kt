package com.youngfeng.android.assistant.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.yanzhenjie.andserver.AndServer
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.db.RoomDatabaseHolder
import com.youngfeng.android.assistant.event.DeviceConnectEvent
import com.youngfeng.android.assistant.event.DeviceDisconnectEvent
import com.youngfeng.android.assistant.event.DeviceReportEvent
import com.youngfeng.android.assistant.manager.DeviceDiscoverManager
import com.youngfeng.android.assistant.model.Command
import com.youngfeng.android.assistant.model.Device
import com.youngfeng.android.assistant.model.MobileInfo
import com.youngfeng.android.assistant.socket.CmdSocketServer
import com.youngfeng.android.assistant.socket.HeartbeatServer
import com.youngfeng.android.assistant.util.CommonUtil
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MobileAssistantApplication : Application() {
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mBatteryReceiver by lazy {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    notifyBatteryChanged()
                }
            }
        }

        receiver
    }
    private val mExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val mHttpServer by lazy(mode = LazyThreadSafetyMode.NONE) {
        AndServer.webServer(this)
            .port(Constants.Port.HTTP_SERVER)
            .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val mGson by lazy { Gson() }

    companion object {
        private lateinit var INSTANCE: MobileAssistantApplication
        private val TAG = MobileAssistantApplication::class.simpleName
        // 默认超时时间（单位：秒）
        private const val DEFAULT_TIMEOUT = 10

        @JvmStatic
        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    private fun notifyBatteryChanged() {
        Log.d(TAG, "notifyBatteryChanged")
        updateMobileInfo()
    }

    override fun onCreate() {
        super.onCreate()
        CmdSocketServer.getInstance().onOpen = {
            updateMobileInfo()
        }
        CmdSocketServer.getInstance().onCommandReceive {
            processCmd(it)
        }
        CmdSocketServer.getInstance().start()

        HeartbeatServer.getInstance().onDeviceConnected = {
            Log.d(TAG, "Device connected.")

            EventBus.getDefault().post(DeviceConnectEvent())
        }
        HeartbeatServer.getInstance().onDeviceDisconnected = {
            Log.d(TAG, "Device disconnected.")

            EventBus.getDefault().post(DeviceDisconnectEvent())
        }
        HeartbeatServer.getInstance().start()

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(mBatteryReceiver, filter)

        mHttpServer.startup()

        DeviceDiscoverManager.getInstance().onDeviceDiscover {
            print("Device: ip => ${it.ip}, name => ${it.name}, platform => ${it.platform}")
        }
        DeviceDiscoverManager.getInstance().startDiscover()

        clearExpiredZipFiles()
    }

    private fun processCmd(cmd: Command<Any>) {
        if (cmd.cmd == Command.CMD_REPORT_DESKTOP_INFO) {
            val str = mGson.toJson(cmd.data)
            val device = mGson.fromJson(str, Device::class.java)

            Log.d(TAG, "Cmd received, cmd: $cmd, device name: ${device.name}")
            EventBus.getDefault().post(DeviceReportEvent(device))
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

    // 获取手机电池电量以及内存使用情况，通知桌面客户端
    private fun updateMobileInfo() {
        val batteryLevel = CommonUtil.getBatteryLevel(this)
        val storageSize = CommonUtil.getExternalStorageSize()

        val mobileInfo = MobileInfo(batteryLevel, storageSize)

        val cmd = Command(Command.CMD_UPDATE_MOBILE_INFO, mobileInfo)
        CmdSocketServer.getInstance().sendCmd(cmd)
    }

    override fun onTerminate() {
        super.onTerminate()

        unregisterReceiver(mBatteryReceiver)
        mExecutorService.shutdownNow()
        mHttpServer.shutdown()
        HeartbeatServer.getInstance().stop()
    }
}
