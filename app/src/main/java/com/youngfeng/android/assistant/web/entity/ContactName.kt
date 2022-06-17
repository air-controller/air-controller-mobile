package com.youngfeng.android.assistant.web.entity

data class ContactName(
    val id: Long,
    val rawContactId: Long,
    val contactId: Long,
    val isPrimary: Boolean,
    val displayName: String?,
    val givenName: String?,
    val middleName: String?,
    val familyName: String?
)
