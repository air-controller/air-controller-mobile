package com.youngfeng.android.assistant.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.youngfeng.android.assistant.model.DesktopInfo
import com.youngfeng.android.assistant.socket.CmdSocketServer
import com.youngfeng.android.assistant.socket.HeartbeatServer

class HomeViewModel : ViewModel() {
    private val _isWifiConnected = MutableLiveData<Boolean>()
    val isWifiConnected: LiveData<Boolean> = _isWifiConnected

    private val _isDeviceConnected = MutableLiveData(false)
    val isDeviceConnected: LiveData<Boolean> = _isDeviceConnected

    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String> = _deviceName

    private val _wlanName = MutableLiveData<String>()
    val wlanName: LiveData<String> = _wlanName

    private val _desktopInfo = MutableLiveData<DesktopInfo>()
    val desktopInfo: LiveData<DesktopInfo> = _desktopInfo

    fun setWifiConnectStatus(isConnected: Boolean) {
        _isWifiConnected.value = isConnected
    }

    fun setDeviceConnected(isConnected: Boolean) {
        _isDeviceConnected.value = isConnected
    }

    fun setDeviceName(deviceName: String) {
        _deviceName.value = deviceName
    }

    fun setWlanName(wlanName: String) {
        _wlanName.value = wlanName
    }

    fun setDesktopInfo(desktopInfo: DesktopInfo) {
        _desktopInfo.value = desktopInfo
    }

    fun disconnect() {
        CmdSocketServer.getInstance().disconnect()
        HeartbeatServer.getInstance().disconnect()
        _isDeviceConnected.value = false
    }
}
