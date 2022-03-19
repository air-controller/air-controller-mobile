package com.youngfeng.android.assistant.socket.heartbeat

open class HeartbeatListener {
    open fun onStart() {}

    open fun onStop() {}

    open fun onClientTimeout(client: HeartbeatClient) {}

    open fun onClientConnected(client: HeartbeatClient?) {}

    open fun onClientDisconnected() {}
}
