package com.numplates.nomera3.modules.feed.domain.usecase

import com.meera.referrals.domain.model.ReferralDataModel
import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepository
import javax.inject.Inject

class GetLastReferralInfoUseCase @Inject constructor(
    private val roadReferralRepository: RoadReferralRepository
) {

    fun invoke(): ReferralDataModel? {
        return roadReferralRepository.getReferralInfo()
    }

}
