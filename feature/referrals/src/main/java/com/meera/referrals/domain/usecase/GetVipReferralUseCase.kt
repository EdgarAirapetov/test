package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import javax.inject.Inject

class GetVipReferralUseCase @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(): Boolean {
        return repository.getVipReferral()
    }

}
