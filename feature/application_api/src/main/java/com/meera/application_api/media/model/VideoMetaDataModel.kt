package com.meera.application_api.media.model

data class VideoMetadataModel(
    val width: Int,
    val height: Int,
    val isRotated: Boolean,
    val bitrate: Int,
    val duration: Long
)
