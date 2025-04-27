package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import javax.inject.Inject

class CheckReferralCodeUseCase @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(code: String): Result<Boolean> {
        return repository.checkReferralCode(code)
    }

}
