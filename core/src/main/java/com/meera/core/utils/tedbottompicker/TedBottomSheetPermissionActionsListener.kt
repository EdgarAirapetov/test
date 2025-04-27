package com.meera.core.utils.tedbottompicker

interface TedBottomSheetPermissionActionsListener {

    fun onGalleryRequestPermissions()

    fun onGalleryOpenSettings()

    fun onCameraRequestPermissions(fromMediaPicker: Boolean)

    fun onCameraOpenSettings()
}
