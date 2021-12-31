package com.youngfeng.android.assistant.model

data class Command<T>(
    val cmd: Int,
    val data: T? = null
) {
    companion object {
        const val CMD_UPDATE_MOBILE_INFO = 1
    }
}
