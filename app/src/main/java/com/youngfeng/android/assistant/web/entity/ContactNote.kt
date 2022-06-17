package com.youngfeng.android.assistant.web.entity

data class ContactNote(
    val id: Long,
    val contactId: Long,
    val note: String?,
    val isPrimary: Boolean,
    val isSuperPrimary: Boolean
)
