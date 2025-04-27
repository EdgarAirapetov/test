package com.meera.core.utils

import android.media.MediaMetadataRetriever
import timber.log.Timber


fun getVideoBitrate(source: String): Long {
    if (source.isEmpty()) return -1
    val mediaMetadataRetriever = MediaMetadataRetriever()
    return try {
        mediaMetadataRetriever.setDataSource(source)
        val bitrateData =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        bitrateData?.toLong() ?: -1
    } catch (exception: Exception) {
        Timber.e(exception)
        -1
    }
}
