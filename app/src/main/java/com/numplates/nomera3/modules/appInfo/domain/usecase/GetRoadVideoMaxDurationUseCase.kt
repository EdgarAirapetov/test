package com.numplates.nomera3.modules.appInfo.domain.usecase

import timber.log.Timber
import javax.inject.Inject

const val DEFAULT_VIDEO_LENGTH_SEC = 60
const val APP_INFO_VIDEO_EDITOR_KEY = "video_editor"
const val VIDEO_CROP_INFO_ROAD_KEY = "road"

class GetRoadVideoMaxDurationUseCase @Inject constructor(
    private val getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase
) {
    fun invoke(): Int {
        try {
            getAppInfoAsyncUseCase.executeBlocking().let { settings ->
                val videoCrops = settings?.appInfo?.findLast {
                    it.name == APP_INFO_VIDEO_EDITOR_KEY
                }?.cropSetting ?: emptyList()

                return videoCrops.firstOrNull { it.name ==  VIDEO_CROP_INFO_ROAD_KEY }?.maxVideoDurationSec ?: DEFAULT_VIDEO_LENGTH_SEC
            }
        } catch (e: Exception) {
            Timber.e("ERROR when getting road video max duration:$e")
            return DEFAULT_VIDEO_LENGTH_SEC
        }
    }
}
