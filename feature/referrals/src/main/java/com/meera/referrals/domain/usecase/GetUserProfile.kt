package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import com.meera.referrals.domain.model.UserProfileModel
import javax.inject.Inject

class GetUserProfile @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(): UserProfileModel {
        return repository.getUserProfile()
    }

}
