package com.youngfeng.android.assistant.manager

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.Device
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.Timer
import java.util.TimerTask

/**
 * 设备发现管理器，用于发现连接设备，使用UDP广播进行局域网搜索.
 *
 * @author Scott Smith 2021/11/20 21:46
 */
interface DeviceDiscoverManager {
    /**
     * 开始设备发现服务.
     */
    fun startDiscover()

    /**
     * @return 设备发现服务启动状态.
     */
    fun isStarted(): Boolean

    /**
     * 设备被发现回调.
     *
     * @param callback 设备发现回调.
     */
    fun onDeviceDiscover(callback: (device: Device) -> Unit)

    /**
     * 停止设备发现服务.
     */
    fun stopDiscover()

    companion object {
        private val instance = DeviceDiscoverManagerImpl()

        @JvmStatic
        fun getInstance() = instance
    }
}

/**
 * 设备发现管理类实现接口.
 */
class DeviceDiscoverManagerImpl : DeviceDiscoverManager {
    private var mDatagramSocket: DatagramSocket? = null
    private var isStarted = false
    private var onDeviceDiscover: ((device: Device) -> Unit)? = null
    private val mTimer by lazy { Timer() }

    override fun startDiscover() {
        if (null == mDatagramSocket) {
            mDatagramSocket = DatagramSocket(Constants.Port.UDP_DEVICE_DISCOVER)
        }

        while (!isStarted) {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)
            mDatagramSocket?.receive(packet)

            val data = String(buffer)
            if (isValidData(data)) {
                print("isValidData: $data")
            } else {
                print("It's not valid, data: $data")
            }
        }

        mTimer.schedule(
            object : TimerTask() {
                override fun run() {
                    sendBroadcastMsg()
                }
            },
            0, 1000
        )

        isStarted = true
    }

    @SuppressLint("WifiManagerLeak")
    private fun sendBroadcastMsg() {
        val wifiManager = MobileAssistantApplication.getInstance().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        val name = Build.MODEL

        val searchCmd = "${Constants.SEARCH_PREFIX}${Constants
            .RANDOM_STR_SEARCH}#${Constants.PLATFORM_ANDROID}#$name#$ip"

        val cmdByteArray = searchCmd.toByteArray()

        val address = InetSocketAddress("255.255.255.255", Constants.Port.UDP_DEVICE_DISCOVER)
        val packet = DatagramPacket(cmdByteArray, cmdByteArray.size, address)

        mDatagramSocket?.send(packet)
    }

    private fun isValidData(data: String): Boolean {
        return data.startsWith("${Constants.SEARCH_PREFIX}${Constants.RANDOM_STR_SEARCH}#")
    }

    override fun isStarted(): Boolean {
        return isStarted
    }

    override fun onDeviceDiscover(callback: (device: Device) -> Unit) {
        this.onDeviceDiscover = callback
    }

    override fun stopDiscover() {
        mDatagramSocket?.close()
        mTimer.cancel()
        isStarted = false
    }
}
