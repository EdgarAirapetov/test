package com.meera.media_controller_implementation.presentation.util

import android.net.Uri
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.Aspect
import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.domain.usecase.ShouldBeTrimmedUseCase
import com.meera.media_controller_implementation.presentation.mapper.MediaEditorCropsBackendKeyMapper
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

private const val DEFAULT_IMAGE_ASPECT_RATIO = 0.75

internal class MediaControllerNewPostNeedEditUtil @Inject constructor(
    getMediaCropInfoUseCase: GetMediaCropInfoUseCase,
    private val shouldBeTrimmedUseCase: ShouldBeTrimmedUseCase,
    private val fileManager: FileManager,
    private val metaDataDelegate: MediaFileMetaDataDelegate
) {
    private val cropInfo = getMediaCropInfoUseCase.invoke()
    private val mediaPlaceMapper = MediaEditorCropsBackendKeyMapper()
        .apply {
            setVideoCrops(cropInfo.videoCrops)
        }

    fun getMaxVideoLengthSec(openPlace: MediaControllerOpenPlace): Int {
        return mediaPlaceMapper.mapToMaxVideoLengthSec(openPlace)
    }

    fun needToEditMedia(uri: Uri, openPlace: MediaControllerOpenPlace): MediaControllerNeedEditResponse {
        return when (fileManager.getMediaType(uri)) {
            FileUtilsImpl.MEDIA_TYPE_IMAGE -> {
                if (getCropInfo(cropInfo.imageCrops, openPlace)?.forceCrop == true) {
                    metaDataDelegate.getImageMetadata(uri.path.orEmpty())?.let { imageMetadata ->
                        determineNeedToEdit(
                            imageMetadata.width,
                            imageMetadata.height,
                            cropInfo.imageCrops,
                            openPlace
                        )
                    } ?: MediaControllerNeedEditResponse.NoNeedToEdit
                } else {
                    MediaControllerNeedEditResponse.NoNeedToEdit
                }
            }

            FileUtilsImpl.MEDIA_TYPE_VIDEO -> {
                val maxVideoDurationSec = mediaPlaceMapper.mapToMaxVideoLengthSec(openPlace)
                val (currentVideoDurationSec, isVideoTooLong) = shouldBeTrimmedUseCase.executeAndReturnCurrentLength(
                    uri,
                    maxVideoDurationSec + 1
                )

                if (isVideoTooLong) {
                    return MediaControllerNeedEditResponse.VideoTooLong(currentVideoDurationSec, maxVideoDurationSec)
                }

                MediaControllerNeedEditResponse.NoNeedToEdit
            }

            else -> MediaControllerNeedEditResponse.NoNeedToEdit
        }
    }

    private fun determineNeedToEdit(
        width: Int,
        height: Int,
        aspects: List<CropInfo>?,
        openPlace: MediaControllerOpenPlace
    ): MediaControllerNeedEditResponse {
        val aspectRatio = width.toDouble() / height.toDouble()
        val result = BigDecimal(aspectRatio).setScale(2, RoundingMode.HALF_EVEN)
        val isImageHorizontal = width > height
        val appConfigAspect = determineMediaAspect(aspects, openPlace) ?: DEFAULT_IMAGE_ASPECT_RATIO
        val ratioResult = when {
            isImageHorizontal && openPlace != MediaControllerOpenPlace.EventPost -> false
            else -> result.toDouble() < appConfigAspect
        }

        return if (ratioResult) {
            MediaControllerNeedEditResponse.NeedToCrop
        } else {
            MediaControllerNeedEditResponse.NoNeedToEdit
        }
    }

    private fun getCropInfo(aspects: List<CropInfo>?, openPlace: MediaControllerOpenPlace): CropInfo? {
        val mediaControllerCropBackendKey = mediaPlaceMapper.map(openPlace)

        return aspects?.find { cropInfo ->
            cropInfo.name == mediaControllerCropBackendKey.name
        }
    }

    private fun determineMediaAspect(aspects: List<CropInfo>?, openPlace: MediaControllerOpenPlace): Double? {
        val cropInfo = getCropInfo(aspects, openPlace)
        return if (cropInfo != null) {
            getAspect(cropInfo.aspectList)
        } else {
            null
        }
    }

    private fun getAspect(aspects: List<Aspect>?): Double? {
        val minAspects = mutableListOf<Double>()
        aspects?.forEach { aspect ->
            val width = aspect.width
            val height = aspect.height
            if (width != null && height != null) {
                val a = width.toDouble() / height
                minAspects.add(a)
            } else {
                return null
            }
        }
        return minAspects.minOrNull()
    }
}
