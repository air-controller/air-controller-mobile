package com.youngfeng.android.assistant.server.entity

data class ContactDataTypeMap(
    val phone: List<ContactDataType>,
    val email: List<ContactDataType>,
    val address: List<ContactDataType>,
    val im: List<ContactDataType>,
    val relation: List<ContactDataType>
)
