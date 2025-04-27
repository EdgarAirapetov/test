package com.numplates.nomera3.modules.feed.domain.usecase

import com.meera.referrals.domain.model.ReferralDataModel
import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepository
import javax.inject.Inject

class LoadAndCacheReferralInfoUseCase @Inject constructor(
    private val roadReferralRepository: RoadReferralRepository
) {

    suspend fun invoke(): ReferralDataModel {
        return roadReferralRepository.loadAndCacheReferral()
    }

}
