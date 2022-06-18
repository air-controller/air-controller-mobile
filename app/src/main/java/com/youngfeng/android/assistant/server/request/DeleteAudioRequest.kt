package com.youngfeng.android.assistant.server.request

data class DeleteAudioRequest(
    val paths: List<String>
) : BaseRequest()
