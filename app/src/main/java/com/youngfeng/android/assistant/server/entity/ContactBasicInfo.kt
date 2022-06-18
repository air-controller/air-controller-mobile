package com.youngfeng.android.assistant.server.entity

data class ContactBasicInfo(
    val id: Long,
    val contactId: Long,
    val phoneNumber: String?,
    val displayNamePrimary: String?
)
