package com.youngfeng.android.assistant.manager

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.Device
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors

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

    private val mExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    companion object {
        private const val TAG = "DeviceDiscoverManager"
    }

    override fun startDiscover() {
        mExecutor.submit {
            if (null == mDatagramSocket) {
                mDatagramSocket = DatagramSocket(Constants.Port.UDP_DEVICE_DISCOVER)
            }

            mTimer.schedule(
                object : TimerTask() {
                    override fun run() {
                        sendBroadcastMsg()
                    }
                },
                0, 1000
            )

            while (!isStarted) {
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)
                mDatagramSocket?.receive(packet)

                val data = String(buffer)
                if (isValidResponse(data)) {
                    val device = convertToDevice(data)
                    Timber.d("当前设备：${device.name}, platform: ${device.platform}, ip address: ${device.ip}")
                } else {
                    Timber.d("It's not valid, data: $data")
                }
            }

            isStarted = true
        }
    }

    private fun convertToDevice(data: String): Device {
        val realInfo = data.replace("${Constants.SEARCH_RES_PREFIX}${Constants.RADNOM_STR_RES_SEARCH}#", "")

        val arr = realInfo.split("#")
        var platform = -1
        if (arr.isNotEmpty()) {
            platform = arr[0].toIntOrNull() ?: -1
        }

        var name = ""
        if (arr.size > 1) {
            name = arr[1]
        }

        var ip = ""
        if (arr.size > 2) {
            ip = arr[2]
        }

        return Device(name, ip, platform)
    }

    private fun isValidResponse(data: String): Boolean {
        return data.startsWith("${Constants.SEARCH_RES_PREFIX}${Constants.RADNOM_STR_RES_SEARCH}#")
    }

    @SuppressLint("WifiManagerLeak")
    private fun sendBroadcastMsg() {
        try {
            val wifiManager = MobileAssistantApplication.getInstance()
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

            val ip = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            val name = Build.MODEL

            val searchCmd = "${Constants.SEARCH_PREFIX}${
            Constants
                .RANDOM_STR_SEARCH
            }#${Constants.PLATFORM_ANDROID}#$name#$ip"

            val cmdByteArray = searchCmd.toByteArray()

            val address = InetSocketAddress("255.255.255.255", Constants.Port.UDP_DEVICE_DISCOVER)
            val packet = DatagramPacket(cmdByteArray, cmdByteArray.size, address)

            Timber.d("Send msg to 255.255.255.255")
            mDatagramSocket?.send(packet)
        } catch (e: Exception) {
            Timber.e(e.message ?: "Unknown error")
        }
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
        mExecutor.shutdownNow()
        isStarted = false
    }
}
