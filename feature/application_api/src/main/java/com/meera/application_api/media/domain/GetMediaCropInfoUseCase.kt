package com.meera.application_api.media.domain

import com.meera.application_api.media.model.MediaCropInfo

interface GetMediaCropInfoUseCase {
    fun invoke(): MediaCropInfo
}
