package com.youngfeng.android.assistant

/**
 * 该类主要用于定义通用常量.
 *
 * @author Scott Smith 2021/11/19 22:52
 */
object Constants {

    object Port {
        const val HTTP_SERVER = 9527
        const val UDP_DEVICE_DISCOVER = 20000
        const val CMD_SERVER = 20001
        const val HEARTBEAT_SERVER = 20002
    }

    const val PLATFORM_ANDROID = 1

    const val SEARCH_PREFIX = "search#"
    const val SEARCH_RES_PREFIX = "search_msg_received#"
    const val RANDOM_STR_SEARCH = "a2w0nuNyiD6vYogF"
    const val RADNOM_STR_RES_SEARCH = "RBIDoKFHLX9frYTh"

    const val KEEP_TEMP_ZIP_FILE_DURATION = 60 * 60 * 1000;
}
