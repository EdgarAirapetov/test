package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.data.entity.MomentPagingParams
import javax.inject.Inject

class GetMomentsPagingParamsBySourceUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    fun invoke(momentsSource: GetMomentDataUseCase.MomentsSource): MomentPagingParams {
        return momentsRepository.getPagingParams(momentsSource = momentsSource)
    }
}
