package com.meera.referrals.domain.usecase

import com.meera.referrals.domain.ReferralRepository
import javax.inject.Inject

class GetAppLinksUniquenameUrl @Inject constructor(
    private val repository: ReferralRepository
) {

    suspend fun invoke(): String {
        return repository.getAppLinksUniquenameUrl()
    }

}
