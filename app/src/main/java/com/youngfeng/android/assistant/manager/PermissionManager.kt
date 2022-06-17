package com.youngfeng.android.assistant.manager

import android.app.Activity
import com.youngfeng.android.assistant.R
import pub.devrel.easypermissions.EasyPermissions

interface PermissionManager {
    fun hasGetAccountsPermission(): Boolean

    fun hasReadContactsPermission(): Boolean

    fun hasWriteContactsPermission(): Boolean

    fun requestReadAccountPermission(requestCode: Int)

    fun requestReadContactsPermission(requestCode: Int)

    fun requestWriteContactPermission(requestCode: Int)

    fun requestMultiplePermissions(requestCode: Int, vararg permissions: String)

    companion object {
        fun with(activity: Activity): PermissionManager {
            return PermissionManagerImpl(activity)
        }
    }
}

class PermissionManagerImpl(private val activity: Activity) : PermissionManager {
    override fun hasGetAccountsPermission(): Boolean {
        return EasyPermissions.hasPermissions(activity, "android.permission.GET_ACCOUNTS")
    }

    override fun hasReadContactsPermission(): Boolean {
        return EasyPermissions.hasPermissions(activity, "android.permission.READ_CONTACTS")
    }

    override fun hasWriteContactsPermission(): Boolean {
        return EasyPermissions.hasPermissions(activity, "android.permission.WRITE_CONTACTS")
    }

    override fun requestReadAccountPermission(requestCode: Int) {
        EasyPermissions.requestPermissions(
            activity,
            activity.getString(R.string.rationale_get_accounts),
            requestCode,
            "android.permission.GET_ACCOUNTS"
        )
    }

    override fun requestReadContactsPermission(requestCode: Int) {
        EasyPermissions.requestPermissions(
            activity,
            activity.getString(R.string.rationale_read_contacts),
            requestCode,
            "android.permission.READ_CONTACTS"
        )
    }

    override fun requestWriteContactPermission(requestCode: Int) {
        EasyPermissions.requestPermissions(
            activity,
            activity.getString(R.string.rationale_write_contacts),
            requestCode,
            "android.permission.WRITE_CONTACTS"
        )
    }

    override fun requestMultiplePermissions(requestCode: Int, vararg permissions: String) {
        EasyPermissions.requestPermissions(
            activity,
            activity.getString(R.string.rationale_permissions),
            requestCode,
            *permissions
        )
    }
}
