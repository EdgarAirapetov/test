package com.meera.application_api.media.model

import com.meera.media_controller_common.CropInfo

data class MediaCropInfo(
    val videoCrops: List<CropInfo>,
    val imageCrops: List<CropInfo>
)
