package com.youngfeng.android.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.ON_AFTER_RELEASE
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import com.google.gson.Gson
import com.yanzhenjie.andserver.AndServer
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.MainActivity
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.event.DeviceConnectEvent
import com.youngfeng.android.assistant.event.DeviceDisconnectEvent
import com.youngfeng.android.assistant.event.DeviceReportEvent
import com.youngfeng.android.assistant.event.RequestDisconnectClientEvent
import com.youngfeng.android.assistant.manager.DeviceDiscoverManager
import com.youngfeng.android.assistant.model.Command
import com.youngfeng.android.assistant.model.Device
import com.youngfeng.android.assistant.model.MobileInfo
import com.youngfeng.android.assistant.socket.CmdSocketServer
import com.youngfeng.android.assistant.socket.heartbeat.HeartbeatClient
import com.youngfeng.android.assistant.socket.heartbeat.HeartbeatListener
import com.youngfeng.android.assistant.socket.heartbeat.HeartbeatServerPlus
import com.youngfeng.android.assistant.util.CommonUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NetworkService : Service() {
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

    private val mHttpServer by lazy(mode = LazyThreadSafetyMode.NONE) {
        AndServer.webServer(this)
            .port(Constants.Port.HTTP_SERVER)
            .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private lateinit var mWakeLock: PowerManager.WakeLock
    private lateinit var heartbeatServer: HeartbeatServerPlus
    private val mGson by lazy { Gson() }

    private companion object {
        const val DEFAULT_TIMEOUT = 10
        const val RC_NOTIFICATION = 0x1001
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()

        mWakeLock = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            ON_AFTER_RELEASE or PARTIAL_WAKE_LOCK,
            "AirController:NetworkService"
        )
        mWakeLock.acquire(60 * 60 * 1000L)

        CmdSocketServer.getInstance().onOpen = {
            updateMobileInfo()
        }
        CmdSocketServer.getInstance().onCommandReceive {
            processCmd(it)
        }
        CmdSocketServer.getInstance().start()

        heartbeatServer = HeartbeatServerPlus.create()
        heartbeatServer.addListener(object : HeartbeatListener() {
            override fun onStart() {
                super.onStart()

                Timber.d("Heartbeat server start success.")
            }

            override fun onStop() {
                super.onStop()

                Timber.d("Heartbeat server stop success.")
            }

            override fun onClientTimeout(client: HeartbeatClient) {
                super.onClientTimeout(client)

                EventBus.getDefault().post(DeviceDisconnectEvent())
                Timber.d("Heartbeat server, onClientTimeout.")
            }

            override fun onClientConnected(client: HeartbeatClient?) {
                super.onClientConnected(client)

                EventBus.getDefault().post(DeviceConnectEvent())
                Timber.d("Heartbeat server, onNewClientJoin.")
            }

            override fun onClientDisconnected() {
                super.onClientDisconnected()
                EventBus.getDefault().post(DeviceDisconnectEvent())
            }
        })
        heartbeatServer.start()

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(mBatteryReceiver, filter)

        mHttpServer.startup()

        DeviceDiscoverManager.getInstance().onDeviceDiscover {
            Timber.d("Device: ip => ${it.ip}, name => ${it.name}, platform => ${it.platform}")
        }
        DeviceDiscoverManager.getInstance().startDiscover()

        registerEventBus()
    }

    private fun notifyBatteryChanged() {
        updateMobileInfo()
    }

    // 获取手机电池电量以及内存使用情况，通知桌面客户端
    private fun updateMobileInfo() {
        val batteryLevel = CommonUtil.getBatteryLevel(this)
        val storageSize = CommonUtil.getExternalStorageSize()

        val mobileInfo = MobileInfo(batteryLevel, storageSize)

        val cmd = Command(Command.CMD_UPDATE_MOBILE_INFO, mobileInfo)
        CmdSocketServer.getInstance().sendCmd(cmd)
    }

    private fun processCmd(cmd: Command<Any>) {
        if (cmd.cmd == Command.CMD_REPORT_DESKTOP_INFO) {
            val str = mGson.toJson(cmd.data)
            val device = mGson.fromJson(str, Device::class.java)

            Timber.d("Cmd received, cmd: $cmd, device name: ${device.name}")
            EventBus.getDefault().post(DeviceReportEvent(device))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRequestDisconnectClient(event: RequestDisconnectClientEvent) {
        if (this::heartbeatServer.isInitialized) {
            this.heartbeatServer.disconnectClient()
        }
    }

    private fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "channelId" + System.currentTimeMillis()
            val channel = NotificationChannel(channelId, resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)

            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
        val nfIntent = Intent(this, MainActivity::class.java)
        nfIntent.action = Intent.ACTION_MAIN
        nfIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, RC_NOTIFICATION, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, RC_NOTIFICATION, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        builder.setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(getString(R.string.working))
            .setWhen(System.currentTimeMillis())
            .setOngoing(false)

        val notification = builder.build()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mWakeLock.release()
        unRegisterEventBus()
        stopForeground(true)
        super.onDestroy()
    }
}
