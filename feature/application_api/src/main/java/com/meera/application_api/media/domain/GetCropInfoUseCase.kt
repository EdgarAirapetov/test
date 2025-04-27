package com.meera.application_api.media.domain

import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_common.MediaControllerOpenPlace

interface GetCropInfoUseCase {

    // Ex: fileType = FileUtilsImpl.MEDIA_TYPE_VIDEO
    fun invoke(fileType: Int, mediaPlace: MediaControllerOpenPlace): CropInfo?
}
