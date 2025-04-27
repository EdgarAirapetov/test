package com.meera.core.extensions

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

fun NotificationManagerCompat.notificationsPermitted() = when {
    this.areNotificationsEnabled().not() -> {
        false
    }

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
        this.notificationChannels.firstOrNull { channel ->
            channel.importance == NotificationManager.IMPORTANCE_NONE
        } == null
    }

    else -> true
}

fun Context.isLocationPermitted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun returnReadExternalStoragePermissionAfter33() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    READ_MEDIA_IMAGES
} else {
    READ_EXTERNAL_STORAGE
}

fun returnWriteExternalStoragePermissionAfter33() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    READ_MEDIA_VIDEO
} else {
    WRITE_EXTERNAL_STORAGE
}

fun Context.isEnabledPermission (permission: String) = ContextCompat.checkSelfPermission(this, permission) == 0

fun externalStoragePermissionAfter33And34() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(READ_MEDIA_VIDEO, READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(READ_MEDIA_VIDEO, READ_MEDIA_IMAGES)
    } else {
        arrayOf(READ_EXTERNAL_STORAGE)
    }
