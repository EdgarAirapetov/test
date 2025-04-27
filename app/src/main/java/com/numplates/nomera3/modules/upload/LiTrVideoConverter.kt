package com.numplates.nomera3.modules.upload

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.MediaTransformer.GRANULARITY_DEFAULT
import com.linkedin.android.litr.TrackTransform
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.linkedin.android.litr.codec.MediaCodecDecoder
import com.linkedin.android.litr.codec.MediaCodecEncoder
import com.linkedin.android.litr.io.MediaExtractorMediaSource
import com.linkedin.android.litr.io.MediaMuxerMediaTarget
import com.linkedin.android.litr.io.MediaSource
import com.linkedin.android.litr.io.MediaTarget
import com.linkedin.android.litr.render.AudioRenderer
import com.linkedin.android.litr.render.GlVideoRenderer
import com.meera.media_controller_common.CropInfo
import com.numplates.nomera3.modules.upload.util.NO_MEDIA_TRACK_INDEX
import com.numplates.nomera3.modules.upload.util.extractHeight
import com.numplates.nomera3.modules.upload.util.extractWidth
import com.numplates.nomera3.modules.upload.util.findTrack
import com.numplates.nomera3.modules.upload.util.getIntegerWithDefault
import timber.log.Timber
import java.util.UUID

private const val VIDEO_MAX_PIXEL_SIZE = 1080
private const val VIDEO_LONG_DURATION_MS = 10_000
private const val VIDEO_MAX_BITRATE_LONG = 8 * 1024 * 1024
private const val VIDEO_MAX_BITRATE_SHORT = 10 * 1024 * 1024
private const val VIDEO_ROTATED = 90
private const val VIDEO_DEFAULT_FRAME_RATE = 30
private const val VIDEO_DEFAULT_I_FRAME_INTERVAL = 2
private const val AUDIO_DEFAULT_BITRATE = 192 * 1024
private const val AUDIO_DEFAULT_SAMPLE_RATE = 48_000
private const val AUDIO_DEFAULT_CHANNEL_COUNT = 1

private data class MetadataInfoContainer(
    val width: Int,
    val height: Int,
    val maxSize: Int,
    val rotation: Int,
    val bitrate: Int,
    val maxBitrate: Int,
    val framerate: Int,
    val iFrameInterval: Int,
    val duration: Long,
    val sampleRate: Int,
    val channelCount: Int,
    val audioBitrate: Int
)

class LiTrVideoConverter : VideoConverter {

    override fun compressVideo(
        context: Context,
        srcUri: Uri,
        destination: String,
        cropInfo: CropInfo?,
        baseListener: VideoConverterListener
    ) {
        val mediaTransformer = MediaTransformer(context.applicationContext)
        val listener = baseListener.toLiTrListener(mediaTransformer)

        val requestId = UUID.randomUUID().toString()

        val metadataRetriever = MediaMetadataRetriever()
        val extractor = MediaExtractor()

        try {
            metadataRetriever.setDataSource(context.applicationContext, srcUri)
            extractor.setDataSource(context.applicationContext, srcUri, null)
        } catch (exception: IllegalArgumentException) {
            listener.onError(
                requestId,
                exception,
                null
            )
            return
        }

        val srcInfo = extractMetadata(
            metadataRetriever,
            extractor,
            cropInfo,
            listener,
            requestId
        ) ?: return
        metadataRetriever.release()
        extractor.release()

        val compressedInfo = getCompressedMetadataInfo(srcInfo)

        val targetVideoFormat = setTargetVideoFormatParameters(compressedInfo)
        val targetAudioFormat = setTargetAudioFormatParameters(compressedInfo)

        startTransform(
            context = context,
            srcUri = srcUri,
            destination = destination,
            mediaTransformer = mediaTransformer,
            listener = listener,
            targetVideoFormat = targetVideoFormat,
            targetAudioFormat = targetAudioFormat
        )
    }

    override fun getMaxBitrate(duration: Long) = if (duration in 1 until VIDEO_LONG_DURATION_MS) {
                VIDEO_MAX_BITRATE_SHORT
            } else {
                VIDEO_MAX_BITRATE_LONG
            }


    private fun extractMetadata(
        mediaMetadataRetriever: MediaMetadataRetriever,
        extractor: MediaExtractor,
        cropInfo: CropInfo?,
        listener: TransformationListener,
        requestId: String
    ): MetadataInfoContainer? {

        val width = mediaMetadataRetriever.extractWidth()
        val height = mediaMetadataRetriever.extractHeight()
        val maxSize = cropInfo?.mediaWidth ?: VIDEO_MAX_PIXEL_SIZE

        val srcRotationData =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val srcBitrateData =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        val srcDurationData =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        if (srcRotationData.isNullOrEmpty() || srcBitrateData.isNullOrEmpty() || srcDurationData.isNullOrEmpty()) {
            listener.onError(
                requestId,
                Exception("Failed to extract video meta-data. (rotation=$srcRotationData, bitrate=$srcBitrateData, duration=$srcDurationData)"),
                null
            )
            return null
        }

        val rotation = srcRotationData.toInt()
        val duration = srcDurationData.toLong()
        val bitrate = srcBitrateData.toInt()
        val maxBitrate = cropInfo?.bitrate ?: getMaxBitrate(duration)

        val videoIndex = extractor.findTrack(true)
        val audioIndex = extractor.findTrack(false)
        extractor.selectTrack(videoIndex)
        if (audioIndex != NO_MEDIA_TRACK_INDEX) extractor.selectTrack(audioIndex)
        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        val videoInputFormat = extractor.getTrackFormat(videoIndex)

        val iFrameInterval = videoInputFormat.getIntegerWithDefault(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_DEFAULT_I_FRAME_INTERVAL)

        var sampleRate = -1
        var channelCount = -1
        var audioBitrate = -1
        if (audioIndex != NO_MEDIA_TRACK_INDEX) {
            val audioInputFormat = extractor.getTrackFormat(audioIndex)
            sampleRate = audioInputFormat.getIntegerWithDefault(MediaFormat.KEY_SAMPLE_RATE, AUDIO_DEFAULT_SAMPLE_RATE)
            channelCount = audioInputFormat.getIntegerWithDefault(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_DEFAULT_CHANNEL_COUNT)
            audioBitrate = audioInputFormat.getIntegerWithDefault(MediaFormat.KEY_BIT_RATE, AUDIO_DEFAULT_BITRATE)
        }

        return MetadataInfoContainer(
            width = width,
            height = height,
            maxSize = maxSize,
            rotation = rotation,
            bitrate = bitrate,
            maxBitrate = maxBitrate,
            framerate = VIDEO_DEFAULT_FRAME_RATE,
            iFrameInterval = iFrameInterval,
            duration = duration,
            sampleRate = sampleRate,
            channelCount = channelCount,
            audioBitrate = audioBitrate
        )
    }

    private fun getCompressedMetadataInfo(
        srcInfo: MetadataInfoContainer
    ): MetadataInfoContainer {

        val (newWidth, newHeight) = generateWidthAndHeight(srcInfo)

        val newBitrate = srcInfo.bitrate.coerceAtMost(srcInfo.maxBitrate)

        return srcInfo.copy(
            width = newWidth,
            height = newHeight,
            bitrate = newBitrate
        )
    }

    private fun startTransform(
        context: Context,
        srcUri: Uri,
        destination: String,
        mediaTransformer: MediaTransformer,
        listener: TransformationListener,
        targetVideoFormat: MediaFormat? = null,
        targetAudioFormat: MediaFormat? = null
    ) {
        val mediaSource: MediaSource =
            MediaExtractorMediaSource(context, srcUri)
        val mediaTarget: MediaTarget = MediaMuxerMediaTarget(
            destination,
            mediaSource.trackCount,
            mediaSource.orientationHint,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )

        val trackCount = mediaSource.trackCount
        val trackTransforms: MutableList<TrackTransform> = ArrayList(trackCount)
        for (track in 0 until trackCount) {
            val sourceMediaFormat = mediaSource.getTrackFormat(track)
            var mimeType: String? = null
            if (sourceMediaFormat.containsKey(MediaFormat.KEY_MIME)) {
                mimeType = sourceMediaFormat.getString(MediaFormat.KEY_MIME)
            }
            val trackTransformBuilder = TrackTransform.Builder(mediaSource, track, mediaTarget)
                .setTargetTrack(track)
            if (mimeType!!.startsWith("video")) {
                trackTransformBuilder.setDecoder(MediaCodecDecoder())
                    .setRenderer(GlVideoRenderer(null))
                    .setEncoder(MediaCodecEncoder())
                    .setTargetFormat(targetVideoFormat)
            } else if (mimeType.startsWith("audio")) {
                val encoder = MediaCodecEncoder()
                trackTransformBuilder.setDecoder(MediaCodecDecoder())
                    .setEncoder(encoder)
                    .setRenderer(AudioRenderer(encoder, null))
                    .setTargetFormat(targetAudioFormat)
            }
            trackTransforms.add(trackTransformBuilder.build())
        }
        mediaTransformer.transform(
            UUID.randomUUID().toString(),
            trackTransforms,
            listener,
            GRANULARITY_DEFAULT
        )
    }

    private fun setTargetVideoFormatParameters(compressedInfo: MetadataInfoContainer): MediaFormat {
        return MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            compressedInfo.width,
            compressedInfo.height
        ).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, compressedInfo.bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, compressedInfo.framerate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, compressedInfo.iFrameInterval)
            setInteger(MediaFormat.KEY_ROTATION, compressedInfo.rotation)
        }
    }

    private fun setTargetAudioFormatParameters(compressedInfo: MetadataInfoContainer): MediaFormat? {
        return if (compressedInfo.sampleRate > 0 && compressedInfo.channelCount > 0 && compressedInfo.audioBitrate > 0) {
            MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                compressedInfo.sampleRate,
                compressedInfo.channelCount
            ).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, compressedInfo.audioBitrate)
            }
        } else {
            null
        }
    }

    private fun generateWidthAndHeight(info: MetadataInfoContainer): Pair<Int, Int> {
        return when {
            info.rotation == VIDEO_ROTATED && info.height > info.maxSize -> {
                val ratio = info.height / info.width.toFloat()
                val newHeight = info.maxSize
                val newWidth = newHeight / ratio
                Pair(newWidth.toInt(), newHeight)
            }
            info.width > info.maxSize -> {
                val ratio = info.width / info.height.toFloat()
                val newWidth = info.maxSize
                val newHeight = newWidth / ratio
                Pair(newWidth, newHeight.toInt())
            }
            else -> {
                Pair(info.width, info.height)
            }
        }
    }

    /**
     * Converts a basic VideoConverterListener to TransformationListener
     * Requires MediaTransformer to release it after the job is done
     */
    private fun VideoConverterListener.toLiTrListener(mediaTransformer: MediaTransformer): TransformationListener =
        object : TransformationListener {
            override fun onStarted(id: String) {
                this@toLiTrListener.onStarted()
            }

            override fun onProgress(id: String, progress: Float) {
                this@toLiTrListener.onProgress(progress)
            }

            override fun onCompleted(
                id: String,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                release()
                trackTransformationInfos?.forEach {
                    Timber.d("encoderCodec=${it.encoderCodec}, decoderCodec=${it.decoderCodec}")
                }
                this@toLiTrListener.onCompleted()
            }

            override fun onCancelled(
                id: String,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                release()
                this@toLiTrListener.onCancelled()
            }

            override fun onError(
                id: String,
                cause: Throwable?,
                trackTransformationInfos: MutableList<TrackTransformationInfo>?
            ) {
                release()
                this@toLiTrListener.onError(cause)
            }

            private fun release() {
                mediaTransformer.release()
            }
        }
}
