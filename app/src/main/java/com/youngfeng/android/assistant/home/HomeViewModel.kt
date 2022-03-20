package com.youngfeng.android.assistant.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.youngfeng.android.assistant.event.RequestDisconnectClientEvent
import com.youngfeng.android.assistant.model.DesktopInfo
import com.youngfeng.android.assistant.model.RunStatus
import com.youngfeng.android.assistant.socket.CmdSocketServer
import org.greenrobot.eventbus.EventBus

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

    private val _currentRunStatus = MutableLiveData<RunStatus>()
    val currentRunStatus: LiveData<RunStatus> = _currentRunStatus

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

    fun updateRunStatus(status: RunStatus) {
        _currentRunStatus.value = status
    }

    fun disconnect() {
        CmdSocketServer.getInstance().disconnect()
        EventBus.getDefault().post(RequestDisconnectClientEvent())
        _isDeviceConnected.value = false
    }
}
