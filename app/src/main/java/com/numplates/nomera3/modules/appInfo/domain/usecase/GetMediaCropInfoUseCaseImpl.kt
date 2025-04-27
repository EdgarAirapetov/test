package com.numplates.nomera3.modules.appInfo.domain.usecase

import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.application_api.media.model.MediaCropInfo
import timber.log.Timber
import javax.inject.Inject

class GetMediaCropInfoUseCaseImpl @Inject constructor(
    private val getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase
) : GetMediaCropInfoUseCase {
    override fun invoke(): MediaCropInfo {
        try {
            getAppInfoAsyncUseCase.executeBlocking().let { settings ->
                val imageCrops = settings?.appInfo?.findLast {
                    it.name == "photo_editor"
                }?.cropSetting ?: emptyList()

                val videoCrops = settings?.appInfo?.findLast {
                    it.name == "video_editor"
                }?.cropSetting ?: emptyList()

                return MediaCropInfo(
                    imageCrops = imageCrops, videoCrops = videoCrops
                )
            }
        } catch (e: Exception) {
            Timber.e("ERROR when getting media crop info:$e")
            return MediaCropInfo(
                imageCrops = emptyList(), videoCrops = emptyList()
            )
        }
    }

}
