package com.numplates.nomera3.modules.upload.util

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.view.Surface
import timber.log.Timber
import java.io.IOException

/**
 * Get a list of [MediaCodecInfo]s that support provided mimeType and [MediaFormat] (if not null)
 */
fun findCodecForFormat(
    encoder: Boolean,
    mimeType: String,
    mediaFormat: MediaFormat?
): List<MediaCodecInfo> {
    val supportedMediaCodecs: MutableList<MediaCodecInfo> = mutableListOf()
    val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
    for (info in mediaCodecList.codecInfos) {
        if (info.isEncoder != encoder) {
            continue
        }
        try {
            val caps = info.getCapabilitiesForType(mimeType)
            if (caps != null && (mediaFormat == null || caps.isFormatSupported(mediaFormat))) {
                Timber.d("found supported codec = ${info.name}")
                supportedMediaCodecs.add(info)
            }
        } catch (exception: IllegalArgumentException) {
            Timber.e(exception)
            Timber.d("${info.name} doesn't support $mimeType")
        }
    }
    return supportedMediaCodecs
}

/**
 * Creates and configures an instance of [MediaCodec] with provided args
 */
@Throws(IOException::class, IllegalStateException::class)
fun createAndConfigureCodec(
    mediaFormat: MediaFormat,
    surface: Surface?,
    isEncoder: Boolean
): MediaCodec {
    var mediaCodec: MediaCodec? = null
    val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME)
    val supportedMediaCodecs = findCodecForFormat(isEncoder, mimeType!!, mediaFormat)
    if (supportedMediaCodecs.isNotEmpty()) {
        mediaCodec = createAndConfigureCodec(
            mediaFormat, surface, isEncoder, supportedMediaCodecs
        )
    }
    return mediaCodec ?: error("Couldn't find supported codec")
}

/**
 * Creates and configures an instance of [MediaCodec] from the list of [MediaCodecInfo]s
 */
@Throws(IllegalStateException::class, IOException::class)
fun createAndConfigureCodec(
    mediaFormat: MediaFormat,
    surface: Surface?,
    isEncoder: Boolean,
    supportedMediaCodecs: List<MediaCodecInfo>
): MediaCodec {
    var mediaCodec: MediaCodec? = null
    for (codecInfo in supportedMediaCodecs) {
        try {
            Timber.d("creating codec ${codecInfo.name}")
            mediaCodec = MediaCodec.createByCodecName(codecInfo.name)
            configureMediaCodec(mediaCodec, mediaFormat, surface, isEncoder)
            break
        } catch (exception: Exception) {
            Timber.d("caught $exception during codec creation/configuration")
            if (mediaCodec != null) {
                mediaCodec.release()
                mediaCodec = null
            }
        }
    }
    if (mediaCodec == null) {
        error("Couldn't create media codec")
    }
    return mediaCodec
}

@Throws(IllegalStateException::class)
fun configureMediaCodec(
    mediaCodec: MediaCodec,
    mediaFormat: MediaFormat,
    surface: Surface?,
    isEncoder: Boolean
) {
    mediaCodec.configure(
        mediaFormat,
        surface,
        null,
        if (isEncoder) MediaCodec.CONFIGURE_FLAG_ENCODE else 0
    )
}

