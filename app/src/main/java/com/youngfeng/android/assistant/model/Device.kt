package com.youngfeng.android.assistant.model

class Device(
    var name: String,
    var ip: String,
    var platform: Int
) {
    companion object {
        const val PLATFORM_MACOS = 3
        const val PLATFORM_LINUX = 4
        const val PLATFORM_WINDOWS = 5
    }
}
