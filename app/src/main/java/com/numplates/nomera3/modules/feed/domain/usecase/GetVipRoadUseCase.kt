package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepository
import javax.inject.Inject

class GetVipRoadUseCase @Inject constructor(
    private val repository: RoadReferralRepository
) {

    suspend fun invoke(): Boolean {
        return repository.getVip()
    }

}
