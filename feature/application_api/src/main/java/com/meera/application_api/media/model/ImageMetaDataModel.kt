package com.meera.application_api.media.model

data class ImageMetadataModel(
    val width: Int,
    val height: Int,
    val isRotated: Boolean,
    val rotationDegrees: Float = 0f
)
