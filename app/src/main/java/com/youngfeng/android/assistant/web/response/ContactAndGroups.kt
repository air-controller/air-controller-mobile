package com.youngfeng.android.assistant.web.response

import com.youngfeng.android.assistant.web.entity.ContactAccount
import com.youngfeng.android.assistant.web.entity.ContactGroup

data class ContactAccountInfo(
    val account: ContactAccount,
    val groups: List<ContactGroup>
)

data class ContactAndGroups(
    val accounts: List<ContactAccountInfo>,
)
