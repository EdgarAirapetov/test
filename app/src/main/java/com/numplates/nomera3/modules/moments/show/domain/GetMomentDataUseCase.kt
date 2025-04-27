package com.numplates.nomera3.modules.moments.show.domain

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import javax.inject.Inject

class GetMomentDataUseCase @Inject constructor(
    private val appSettings: AppSettings,
    private val momentsRepository: MomentsRepository,
) {

    suspend fun invoke(
        getFromCache: Boolean,
        userId: Long? = null,
        targetMomentId: Long? = null,
        momentsSource: MomentsSource
    ): MomentInfoModel {
        val momentInfoModel = when {
            getFromCache -> momentsRepository.getMomentsFromCache(momentsSource)
            userId != null -> momentsRepository.getMomentsFromRest(userId, targetMomentId, momentsSource)
            else -> momentsRepository.getMomentsFromRest(appSettings.readUID(), targetMomentId, momentsSource)
        }
        return momentInfoModel
    }

    enum class MomentsSource(val value: String) {

        Main("main"),
        User("user"),
        Subscription("subscriptions");

        companion object {
            fun MomentsSource.toRoadTypesEnum(): RoadTypesEnum? {
                return when (this) {
                    Main -> RoadTypesEnum.MAIN
                    Subscription -> RoadTypesEnum.SUBSCRIPTION
                    else -> null
                }
            }
        }
    }
}
