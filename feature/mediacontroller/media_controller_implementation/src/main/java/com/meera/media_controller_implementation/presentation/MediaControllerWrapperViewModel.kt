package com.meera.media_controller_implementation.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.application_api.media.domain.ShouldForceResizeVideoUseCase
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_common.MediaEditorResult
import com.meera.media_controller_implementation.presentation.mapper.MediaEditorCropsBackendKeyMapper
import com.meera.media_controller_implementation.presentation.util.AspectRatioMapper
import com.meera.media_controller_implementation.presentation.util.ForceCropMapper
import com.meera.media_controller_implementation.presentation.util.MediaEditorResultMapper
import com.noomeera.nmrmediasdk.NMRMediaSDK
import com.noomeera.nmrmediatools.NMRResult
import com.noomeera.nmrmediatools.utils.CropMode
import com.noomeera.nmrmediatools.utils.NMRMediaSettings
import com.noomeera.nmrmediatools.utils.Ratio
import javax.inject.Inject

/**
 * Так как обрезка видео не может быть выполнена с точностью до миллисекунды
 * то необходимо добавить "буфферную зону" чтобы избежать ситуацию когда обрезка видео до 20 секунд
 * приводит к видео длинной 20.010 секунд (что мешает такому видео пройти проверку на длительность)
 */
private val DEFAULT_RATIO = listOf(Ratio(1, 1))
private const val DEFAULT_VIDEO_BITRATE = 3_500_000
private const val DEFAULT_MOMENT_HEIGHT = 1280
const val DEFAULT_IMAGE_QUALITY = 80
const val DEFAULT_MEDIA_WIDTH = 1080

internal class MediaControllerWrapperViewModel @Inject constructor(
    getMediaCropInfoUseCase: GetMediaCropInfoUseCase,
    private val shouldForceResizeVideoUseCase: ShouldForceResizeVideoUseCase,
    private val filesManager: FileManager
) : ViewModel() {

    private val cropInfo = getMediaCropInfoUseCase.invoke()
    private val ratioMapper = AspectRatioMapper(DEFAULT_RATIO)
    private val forceCropMapper = ForceCropMapper()
    private val mediaResultMapper = MediaEditorResultMapper()
    private val mediaPlaceMapper = MediaEditorCropsBackendKeyMapper()

    init {
        mediaPlaceMapper.setVideoCrops(cropInfo.videoCrops)
    }

    fun deleteFile(filePath: String?) = filesManager.deleteFile(filePath)

    fun getOpenEditorEvent(
        uri: Uri?,
        openPlace: MediaControllerOpenPlace
    ): MediaControllerWrapperEvent? {
        val mediaSettings = getMediaSettings()

        if (openPlace == MediaControllerOpenPlace.Moments) {
            return MediaControllerWrapperEvent.OpenMomentsExternalEditor(mediaSettings)
        }
        val mediaType = getMediaType(uri) ?: let {
            FirebaseCrashlytics.getInstance()
                .recordException(IllegalStateException("Невозможно открыть медиа-редактор. Невозможно определить тип файла: $uri"))
            return null
        }

        val ratios = getRatios(mediaType, openPlace)
        val cropMode = getCropMode(mediaType, openPlace)
        val maxDurationMs = mediaPlaceMapper.mapToMaxVideoLengthMs(openPlace)
        val forceResize = mediaType == NMRMediaSDK.MediaType.VIDEO && getForceResizeVideo()
        return MediaControllerWrapperEvent.OpenExternalEditor(
            uri,
            ratios,
            mediaType,
            maxDurationMs,
            cropMode,
            forceResize,
            mediaSettings
        )
    }

    fun getMediaResults(results: List<NMRResult>): ArrayList<MediaEditorResult> {
        return ArrayList(results.map { result ->
            mediaResultMapper.map(result)
        })
    }

    private fun getMediaSettings(): NMRMediaSettings {
        val momentImageInfo = getCropInfo(
            mediaType = NMRMediaSDK.MediaType.IMAGE,
            openPlace = MediaControllerOpenPlace.Moments
        )
        val postVideoInfo = getCropInfo(
            mediaType = NMRMediaSDK.MediaType.VIDEO,
            openPlace = MediaControllerOpenPlace.Post
        )
        return NMRMediaSettings(
            imageQuality = momentImageInfo?.imageQuality ?: DEFAULT_IMAGE_QUALITY,
            videoBitrate = postVideoInfo?.bitrate ?: DEFAULT_VIDEO_BITRATE,
            momentMediaHeight = momentImageInfo?.mediaHeight ?: DEFAULT_MOMENT_HEIGHT,
            postMediaWidth = postVideoInfo?.mediaWidth ?: DEFAULT_MEDIA_WIDTH
        )
    }

    private fun getForceResizeVideo(): Boolean {
        return shouldForceResizeVideoUseCase.invoke()
    }

    private fun getRatios(mediaType: NMRMediaSDK.MediaType, openPlace: MediaControllerOpenPlace): List<Ratio> {
        return ratioMapper.map(getCropInfo(mediaType, openPlace))
    }

    private fun getCropMode(mediaType: NMRMediaSDK.MediaType, openPlace: MediaControllerOpenPlace): CropMode {
        val cropInfo = getCropInfo(mediaType, openPlace)

        return forceCropMapper.map(
            cropInfo?.forceCrop,
            openPlace == MediaControllerOpenPlace.Avatar
        )
    }

    private fun getCropInfo(mediaType: NMRMediaSDK.MediaType, openPlace: MediaControllerOpenPlace): CropInfo? {
        return when (mediaType) {
            NMRMediaSDK.MediaType.IMAGE -> {
                findMediaPlaceInCropList(cropInfo.imageCrops, openPlace)
            }

            NMRMediaSDK.MediaType.VIDEO -> {
                findMediaPlaceInCropList(cropInfo.videoCrops, openPlace)
            }
        }
    }

    private fun findMediaPlaceInCropList(
        mediaTypeCropList: List<CropInfo>,
        openPlace: MediaControllerOpenPlace
    ): CropInfo? {
        val mediaControllerCropBackendKey = mediaPlaceMapper.map(openPlace)

        return mediaTypeCropList.find { it.name == mediaControllerCropBackendKey.name }
    }

    private fun getMediaType(uri: Uri?): NMRMediaSDK.MediaType? {
        return when (filesManager.getMediaType(uri)) {
            FileUtilsImpl.MEDIA_TYPE_IMAGE -> NMRMediaSDK.MediaType.IMAGE
            FileUtilsImpl.MEDIA_TYPE_IMAGE_GIF -> NMRMediaSDK.MediaType.IMAGE
            FileUtilsImpl.MEDIA_TYPE_VIDEO -> NMRMediaSDK.MediaType.VIDEO
            FileUtilsImpl.MEDIA_TYPE_UNKNOWN -> null
            else -> null
        }
    }
}
