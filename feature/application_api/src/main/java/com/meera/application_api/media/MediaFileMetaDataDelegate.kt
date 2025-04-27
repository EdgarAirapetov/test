package com.meera.application_api.media

import android.net.Uri
import com.meera.application_api.media.model.ImageMetadataModel
import com.meera.application_api.media.model.VideoMetadataModel

interface MediaFileMetaDataDelegate {
    fun getImageMetadata(path: String): ImageMetadataModel?
    fun getVideoMetadata(uri: Uri): VideoMetadataModel?
    fun isGifImage(path: String): Boolean
}
