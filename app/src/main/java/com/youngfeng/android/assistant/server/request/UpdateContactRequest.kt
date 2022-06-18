package com.youngfeng.android.assistant.server.request

import com.youngfeng.android.assistant.server.entity.ContactAccount
import com.youngfeng.android.assistant.server.entity.ContactFieldItem
import com.youngfeng.android.assistant.server.entity.ContactGroup

data class UpdateContactRequest(
    val id: Long = -1,
    val name: String = "",
    val account: ContactAccount? = null,
    val group: ContactGroup? = null,
    val phones: List<ContactFieldItem>? = null,
    val emails: List<ContactFieldItem>? = null,
    val ims: List<ContactFieldItem>? = null,
    val addresses: List<ContactFieldItem>? = null,
    val relations: List<ContactFieldItem>? = null,
    val note: String? = null
)
