package com.youngfeng.android.assistant.web.request

import com.youngfeng.android.assistant.web.entity.ContactAccount
import com.youngfeng.android.assistant.web.entity.ContactFieldItem
import com.youngfeng.android.assistant.web.entity.ContactGroup

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
