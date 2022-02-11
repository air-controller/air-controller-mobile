package com.youngfeng.android.assistant

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.youngfeng.android.assistant.databinding.ActivityMainBinding
import com.youngfeng.android.assistant.event.DeviceConnectEvent
import com.youngfeng.android.assistant.event.DeviceDisconnectEvent
import com.youngfeng.android.assistant.event.DeviceReportEvent
import com.youngfeng.android.assistant.home.HomeViewModel
import com.youngfeng.android.assistant.model.DesktopInfo
import com.youngfeng.android.assistant.model.Device
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private var mViewDataBinding: ActivityMainBinding? = null
    private val mViewModel by viewModels<HomeViewModel>()
    private val mDisconnectConfirmDialog by lazy {
        AlertDialog.Builder(this)
            .setPositiveButton(
                R.string.sure
            ) { _, _ -> mViewModel.disconnect() }
            .setNegativeButton(R.string.cancel) {
                dialog, _ ->
                dialog.dismiss()
            }.setTitle(R.string.tip_disconnect)
            .create()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mViewDataBinding?.apply {
            this.lifecycleOwner = this@MainActivity
            this.textSupportDeveloper.paint.flags = Paint.UNDERLINE_TEXT_FLAG
            this.viewModel = mViewModel

            this.btnOpenWifiSettings.setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "Open wifi settings fail.")
                }
            }

            this.btnDisconnect.setOnClickListener {
                if (!mDisconnectConfirmDialog.isShowing) {
                    mDisconnectConfirmDialog.show()
                }
            }
        }

        registerNetworkListener()
        setUpDeviceInfo()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun registerNetworkListener() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    runOnUiThread {
                        val isWifiConnected = connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        mViewModel.setWifiConnectStatus(isWifiConnected == true)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)

                    runOnUiThread {
                        mViewModel.setWifiConnectStatus(false)
                    }
                }
            })
        } else {
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)

                        runOnUiThread {
                            val isWifiConnected = connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            mViewModel.setWifiConnectStatus(isWifiConnected == true)
                        }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)

                        runOnUiThread {
                            mViewModel.setWifiConnectStatus(false)
                        }
                    }
                }
            )
        }
    }

    private fun setUpDeviceInfo() {
        mViewModel.setDeviceName(Build.MODEL)

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid
        mViewModel.setWlanName(ssid?.replace(oldValue = "\"", newValue = "") ?: "Unknown SSID")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnected(event: DeviceConnectEvent) {
        mViewModel.setDeviceConnected(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceDisconnected(event: DeviceDisconnectEvent) {
        mViewModel.setDeviceConnected(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceReport(event: DeviceReportEvent) {
        var os = "MacOS"

        when (event.device.platform) {
            Device.PLATFORM_LINUX -> os = "Linux"
            Device.PLATFORM_WINDOWS -> os = "Windows"
            else -> "MacOS"
        }
        val desktopInfo = DesktopInfo(event.device.name, event.device.ip, os)
        mViewModel.setDesktopInfo(desktopInfo)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
