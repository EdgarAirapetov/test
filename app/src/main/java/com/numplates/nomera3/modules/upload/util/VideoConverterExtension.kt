package com.numplates.nomera3.modules.upload.util

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import timber.log.Timber

private const val MIN_HEIGHT = 640
private const val MIN_WIDTH = 368
const val NO_MEDIA_TRACK_INDEX = -5

fun MediaExtractor.findTrack(video: Boolean): Int {
    val numTracks = trackCount
    for (i in 0 until numTracks) {
        val format = getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        when {
            video && mime?.startsWith("video/") == true -> return i
            video.not() && mime?.startsWith("audio/") == true -> return i
        }
    }
    return NO_MEDIA_TRACK_INDEX
}

fun MediaFormat.getIntegerWithDefault(key: String, default: Int): Int {
    return try {
        getInteger(key)
    } catch (e: Exception) {
        Timber.e(e)
        default
    }
}

fun MediaMetadataRetriever.extractWidth(): Int {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: MIN_WIDTH
}

fun MediaMetadataRetriever.extractHeight(): Int {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: MIN_HEIGHT
}
