package com.youngfeng.android.assistant.model

enum class RunStatus {
    /**
     * 未连接到桌面端
     */
    Disconnected,

    /**
     * 全部功能运行正常
     */
    Normal,

    /**
     * 部分功能运行正常
     */
    PartNormal,

    /**
     * 全部功能运行不正常
     */
    AllNotWorking
}
