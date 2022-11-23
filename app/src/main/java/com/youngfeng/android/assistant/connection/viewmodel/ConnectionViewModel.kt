
package com.youngfeng.android.assistant.connection.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.youngfeng.android.assistant.manager.AccessControlManager

class ConnectionViewModel : ViewModel() {
    private val _ipAddress = MutableLiveData<String>()
    val ipAddress: LiveData<String> = _ipAddress

    private val _allowAccess = MutableLiveData<Boolean>()
    val allowAccess: LiveData<Boolean> = _allowAccess

    private val _enablePwd = MutableLiveData<Boolean>()
    val enablePwd: LiveData<Boolean> = _enablePwd

    private val _showPwd = MutableLiveData<Boolean>()
    val showPwd: LiveData<Boolean> = _showPwd

    val passwd: MutableLiveData<String> = MutableLiveData()

    fun init() {
        _allowAccess.value = AccessControlManager.getInstance().allowAccess
        _enablePwd.value = AccessControlManager.getInstance().enablePwd
        _showPwd.value = false
        passwd.value = AccessControlManager.getInstance().password
    }

    fun setIpAddress(ipAddress: String) {
        _ipAddress.value = ipAddress
    }

    fun setAllowAccess(allowAccess: Boolean) {
        AccessControlManager.getInstance().allowAccess = allowAccess
        _allowAccess.value = allowAccess
    }

    fun setEnablePwd(enablePwd: Boolean) {
        AccessControlManager.getInstance().enablePwd = enablePwd
        _enablePwd.value = enablePwd
    }

    fun switchPwdVisibility() {
        _showPwd.value = !(_showPwd.value ?: false)
    }
}
