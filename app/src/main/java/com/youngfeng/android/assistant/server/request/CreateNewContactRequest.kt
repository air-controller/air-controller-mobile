package com.youngfeng.android.assistant.server.request

import com.youngfeng.android.assistant.server.entity.ContactAccount
import com.youngfeng.android.assistant.server.entity.ContactFieldItem
import com.youngfeng.android.assistant.server.entity.ContactGroup

open class CreateNewContactRequest(
    val name: String,
    val account: ContactAccount?,
    val group: ContactGroup?,
    val phones: List<ContactFieldItem>?,
    val emails: List<ContactFieldItem>?,
    val ims: List<ContactFieldItem>?,
    val addresses: List<ContactFieldItem>?,
    val relations: List<ContactFieldItem>?,
    val note: String?
)
