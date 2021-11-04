package com.youngfeng.android.assistant.ext

/**
 * @return 该字符串是否是一个有效的文件名
 */
fun String.isValidFileName(): Boolean {
    return !this.contains("/")
}