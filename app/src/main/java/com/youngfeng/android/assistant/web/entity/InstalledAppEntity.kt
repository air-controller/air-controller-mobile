package com.youngfeng.android.assistant.web.entity

data class InstalledAppEntity(
    val isSystemApp: Boolean,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val size: Long,
    val enable: Boolean
)
