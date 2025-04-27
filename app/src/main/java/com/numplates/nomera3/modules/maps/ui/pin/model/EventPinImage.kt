package com.numplates.nomera3.modules.maps.ui.pin.model

import android.graphics.Bitmap

sealed interface EventPinImage {
    data class ImageLoaded(val bitmap: Bitmap) : EventPinImage
    object ImageError : EventPinImage
    object NoImage : EventPinImage
}
