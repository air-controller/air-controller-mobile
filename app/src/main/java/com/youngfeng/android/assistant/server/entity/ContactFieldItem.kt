package com.youngfeng.android.assistant.server.entity

data class ContactFieldItem(
    var id: Long = -1,
    var type: ContactDataType? = null,
    var value: String? = null
)
