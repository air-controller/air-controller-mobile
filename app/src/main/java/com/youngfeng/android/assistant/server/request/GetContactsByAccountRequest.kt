package com.youngfeng.android.assistant.server.request

data class GetContactsByAccountRequest(
    val name: String,
    val type: String
)
