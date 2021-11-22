package com.youngfeng.android.assistant.web.request

data class DeleteAlbumsRequest(
    val paths: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeleteAlbumsRequest

        if (!paths.contentEquals(other.paths)) return false

        return true
    }

    override fun hashCode(): Int {
        return paths.contentHashCode()
    }
}
