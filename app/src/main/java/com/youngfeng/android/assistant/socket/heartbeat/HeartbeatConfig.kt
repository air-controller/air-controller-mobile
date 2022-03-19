package com.youngfeng.android.assistant.socket.heartbeat

enum class HeartbeatStrategy {
    /**
     * 在心跳过程中，如果有新客户端连接，继续保持当前心跳连接，不做任何处理
     */
    KeepConnectWhenNewJoin,

    /**
     * 在心跳过程中，如果有新客户端连接，则踢掉当前连接，连接到新客户端
     */
    ConnectToNewWhenJoin
}

data class HeartbeatConfig(
    var allowRetry: Boolean = true,
    var strategy: HeartbeatStrategy = HeartbeatStrategy.ConnectToNewWhenJoin,
    var timeoutInMills: Int = 3000,
    var retryWaitTimeInMills: Int = 12000,
    var disconnectClientWhenTimeout: Boolean = true
)
