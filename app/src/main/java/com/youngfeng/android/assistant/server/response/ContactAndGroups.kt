package com.youngfeng.android.assistant.server.response

import com.youngfeng.android.assistant.server.entity.ContactAccount
import com.youngfeng.android.assistant.server.entity.ContactGroup

data class ContactAccountInfo(
    val account: ContactAccount,
    val groups: List<ContactGroup>
)

data class ContactAndGroups(
    val accounts: List<ContactAccountInfo>,
)
