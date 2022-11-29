package com.youngfeng.android.assistant.server.entity

enum class DeleteResult {
    SUCCESS,
    FAILED,
    PARTIAL
}

data class DeleteResultEntity(
    val result: DeleteResult,
    val failedCount: Int = 0
) {
    companion object {
        fun success() = DeleteResultEntity(DeleteResult.SUCCESS)
        fun failed() = DeleteResultEntity(DeleteResult.FAILED)
        fun partial(failedCount: Int) = DeleteResultEntity(DeleteResult.PARTIAL, failedCount)
    }
}
