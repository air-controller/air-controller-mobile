package com.youngfeng.android.assistant.web.entity

data class ContactBasicInfo(
    val id: Long,
    val contactId: Long,
    val phoneNumber: String?,
    val displayNamePrimary: String?
)
