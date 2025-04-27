package com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository.ProfileStatisticsRepository
import javax.inject.Inject

class SetProfileStatisticsSlidesUseCase @Inject constructor(
    private val repository: ProfileStatisticsRepository
) : BaseUseCaseNoSuspend<SetProfileStatisticsParams, Unit> {

    override fun execute(params: SetProfileStatisticsParams) {
        repository.setSlides(params.slidesListResponse)
    }
}

class SetProfileStatisticsParams(val slidesListResponse: SlidesListResponse?) : DefParams()
