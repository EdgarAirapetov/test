package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import javax.inject.Inject

class SetMomentsPagingParamsBySourceUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    fun invoke(
        momentsSource: GetMomentDataUseCase.MomentsSource,
        momentPagingParams: MomentPagingParams
    ) {
        momentsRepository.setPagingParams(
            momentsSource = momentsSource,
            momentPagingParams = momentPagingParams
        )
    }
}
