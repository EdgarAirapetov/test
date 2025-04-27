package com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspendWithoutParams
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository.ProfileStatisticsRepository
import javax.inject.Inject

class GetProfileStatisticsSlidesUseCase @Inject constructor(
    private val repository: ProfileStatisticsRepository
) : BaseUseCaseNoSuspendWithoutParams<SlidesListResponse?> {

    override fun execute(): SlidesListResponse? {
        return repository.getSlides()
    }
}