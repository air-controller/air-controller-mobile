package com.youngfeng.android.assistant.server.entity

data class ContactNote(
    val id: Long,
    val contactId: Long,
    val note: String?,
    val isPrimary: Boolean,
    val isSuperPrimary: Boolean
)
