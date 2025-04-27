package com.numplates.nomera3.modules.feed.domain.usecase

import com.meera.referrals.domain.model.UserProfileModel
import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepository
import javax.inject.Inject

class GetUserProfileForVipUseCase @Inject constructor(
    private val roadReferralRepository: RoadReferralRepository
) {

    suspend fun invoke(): UserProfileModel {
        return roadReferralRepository.getUserProfile()
    }

}
