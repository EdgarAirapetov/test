package com.meera.core.utils.tedbottompicker.compat

import android.net.Uri

interface OnImagesReady {

    fun onReady(images: MutableList<Uri>?) = Unit

    fun onReady(image: Uri) = Unit

    fun onReadyWithText(images: MutableList<out Uri>?, text: String?) {}

    fun onProgress() {}

    fun onDismiss() {}

    fun onRequestChangeState(bottomSheetState: Int) {}
}
