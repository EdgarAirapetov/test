package com.meera.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import javax.inject.Inject

class ReadContactsPermissionProvider @Inject constructor(
    private val context: Context
) {
    fun hasContactsPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        return permission == PackageManager.PERMISSION_GRANTED
    }
}
