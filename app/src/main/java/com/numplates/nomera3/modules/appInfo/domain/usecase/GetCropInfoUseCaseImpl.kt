package com.numplates.nomera3.modules.appInfo.domain.usecase

import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.presentation.mapper.MediaEditorCropsBackendKeyMapper
import javax.inject.Inject

class GetCropInfoUseCaseImpl @Inject constructor(
    private val getMediaCropInfoUseCase: GetMediaCropInfoUseCase,
) : GetCropInfoUseCase {

    private val mediaPlaceMapper = MediaEditorCropsBackendKeyMapper()

    override fun invoke(
        fileType: Int,
        mediaPlace: MediaControllerOpenPlace
    ): CropInfo? = when (fileType) {

        FileUtilsImpl.MEDIA_TYPE_IMAGE -> {
            getMediaCropInfoUseCase.invoke().imageCrops
        }

        FileUtilsImpl.MEDIA_TYPE_VIDEO -> {
            getMediaCropInfoUseCase.invoke().videoCrops
        }

        else -> null
    }?.let {
        findMediaPlaceInCropList(it, mediaPlace)
    }

    private fun findMediaPlaceInCropList(
        mediaTypeCropList: List<CropInfo>,
        openPlace: MediaControllerOpenPlace
    ): CropInfo? {
        val mediaControllerCropBackendKey = mediaPlaceMapper.map(openPlace)
        return mediaTypeCropList.find { it.name == mediaControllerCropBackendKey.name }
    }

}
