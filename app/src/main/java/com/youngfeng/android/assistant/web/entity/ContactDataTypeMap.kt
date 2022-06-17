package com.youngfeng.android.assistant.web.entity

data class ContactDataTypeMap(
    val phone: List<ContactDataType>,
    val email: List<ContactDataType>,
    val address: List<ContactDataType>,
    val im: List<ContactDataType>,
    val relation: List<ContactDataType>
)
