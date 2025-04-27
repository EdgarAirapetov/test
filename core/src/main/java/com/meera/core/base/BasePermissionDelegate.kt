package com.meera.core.base

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.meera.core.permission.PermissionDelegate

interface BasePermission {

    var permissionDelegate: PermissionDelegate?

    fun initPermissionDelegate(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner
    )

    fun setPermissions(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    )

    fun setPermissionsWithSettingsOpening(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    )

    fun checkPermissions(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    )

    fun setMediaPermissions()

    fun checkMediaPermissions(listener: PermissionDelegate.Listener?)
}

class BasePermissionDelegate : BasePermission {

    override var permissionDelegate: PermissionDelegate? = null

    override fun initPermissionDelegate(activity: Activity, viewLifecycleOwner: LifecycleOwner) {
        permissionDelegate = PermissionDelegate(
            activity = activity,
            viewLifecycleOwner = viewLifecycleOwner
        )
    }

    override fun setPermissions(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    ) {
        permissionDelegate?.setPermissions(
            listener = listener,
            permission = permission,
            permissions = permissions
        )
    }

    override fun setMediaPermissions() {
        permissionDelegate?.setMediaPermissions()
    }

    override fun checkMediaPermissions(listener: PermissionDelegate.Listener?) {
        permissionDelegate?.checkMediaPermissions(listener)
    }

    override fun setPermissionsWithSettingsOpening(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    ) {
        permissionDelegate?.setPermissionsWithSettingsOpening(
            listener = listener,
            permission = permission,
            permissions = permissions
        )
    }

    override fun checkPermissions(
        listener: PermissionDelegate.Listener,
        permission: String,
        vararg permissions: String
    ) {
        permissionDelegate?.checkPermissions(
            listener = listener,
            permission = permission,
            permissions = permissions
        )
    }

}
