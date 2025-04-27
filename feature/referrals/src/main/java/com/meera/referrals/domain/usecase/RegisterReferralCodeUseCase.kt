package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import javax.inject.Inject

class RegisterReferralCodeUseCase @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(code: String): Boolean {
        return repository.registerReferralCode(code)
    }

}
