package com.meera.referrals.domain.usecase

import com.meera.referrals.data.repository.ReferralRepositoryImpl
import javax.inject.Inject

class GetAccountTypeUseCase @Inject constructor(
    private val repository: ReferralRepositoryImpl
) {

    fun invoke(): Int = repository.getAccountType()

}
