package com.youngfeng.android.assistant.server.request

data class DeleteMultiImageRequest(val ids: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeleteMultiImageRequest

        if (!ids.contentEquals(other.ids)) return false

        return true
    }

    override fun hashCode(): Int {
        return ids.contentHashCode()
    }
}
