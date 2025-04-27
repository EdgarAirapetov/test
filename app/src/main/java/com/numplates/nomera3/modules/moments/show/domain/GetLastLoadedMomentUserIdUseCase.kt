package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class GetLastLoadedMomentUserIdUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository,
) {
    fun invoke(momentsSource: GetMomentDataUseCase.MomentsSource): Int {
        return momentsRepository.getLastLoadedMomentUserId(momentsSource)
    }
}
