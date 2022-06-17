package com.youngfeng.android.assistant.web.entity

data class ContactDetail(
    val id: Long,
    val contactId: Long,
    val displayNamePrimary: String? = null,
    val accounts: List<ContactAccount>? = null,
    val groups: List<ContactGroup>? = null,
    val phones: List<ContactFieldItem>? = null,
    val emails: List<ContactFieldItem>? = null,
    val addresses: List<ContactFieldItem>? = null,
    val ims: List<ContactFieldItem>? = null,
    val relations: List<ContactFieldItem>? = null,
    val note: ContactNote? = null
)
