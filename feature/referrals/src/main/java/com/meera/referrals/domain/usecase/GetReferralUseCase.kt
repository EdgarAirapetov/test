package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import com.meera.referrals.domain.model.ReferralDataModel
import javax.inject.Inject

class GetReferralUseCase @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(): ReferralDataModel {
        return repository.getReferral()
    }

}
