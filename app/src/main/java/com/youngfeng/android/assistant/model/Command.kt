package com.youngfeng.android.assistant.model

data class Command<T>(
    val cmd: Int,
    val data: T? = null
) {
    companion object {
        const val CMD_UPDATE_MOBILE_INFO = 1
        // 桌面端上报设备信息
        const val CMD_REPORT_DESKTOP_INFO = 2
        // 桌面端请求断开连接
        const val CMD_REQUEST_DISCONNECT = 3
        // 手机端断开连接
        const val CMD_DISCONNECT = 4
    }
}
