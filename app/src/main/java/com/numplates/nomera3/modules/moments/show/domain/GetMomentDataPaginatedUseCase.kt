package com.numplates.nomera3.modules.moments.show.domain

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import javax.inject.Inject

const val DEFAULT_MOMENTS_PAGE_LIMIT = 10

class GetMomentDataPaginatedUseCase @Inject constructor(
    private val appSettings: AppSettings,
    private val momentsRepository: MomentsRepository,
) {

    suspend fun invoke(
        momentsSource: GetMomentDataUseCase.MomentsSource,
        isFromCache: Boolean = false,
        sessionId: String? = null,
        startId: Int = 0,
        limit: Int = DEFAULT_MOMENTS_PAGE_LIMIT
    ): MomentInfoModel {
        val userId = appSettings.readUID()
        val oldMomentInfo = momentsRepository.getMomentsFromCache(momentsSource)
        val oldMomentsSize = if (sessionId != null) oldMomentInfo.momentGroups.size else 0
        if (isFromCache) return oldMomentInfo.copy(lastPageSize = oldMomentsSize)
        val paginatedMomentInfo = momentsRepository.getMomentsPaginated(
            userId = userId,
            startId = startId,
            limit = limit,
            momentsSource = momentsSource,
            sessionId = sessionId
        )
        return paginatedMomentInfo.copy(lastPageSize = paginatedMomentInfo.momentGroups.size - oldMomentsSize)
    }
}
