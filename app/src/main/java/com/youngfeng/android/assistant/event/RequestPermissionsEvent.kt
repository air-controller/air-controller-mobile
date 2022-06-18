package com.youngfeng.android.assistant.event

enum class Permission {
    GetAccounts,
    ReadContacts,
    WriteContacts,
    RequestInstallPackages
}

data class RequestPermissionsEvent(val permissions: Array<Permission>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestPermissionsEvent

        if (!permissions.contentEquals(other.permissions)) return false

        return true
    }

    override fun hashCode(): Int {
        return permissions.contentHashCode()
    }
}
