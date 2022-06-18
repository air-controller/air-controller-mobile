package com.youngfeng.android.assistant.server.entity

data class DailyImageEntity(
    var start: Long,
    var end: Long,
    var images: List<ImageEntity>
)
