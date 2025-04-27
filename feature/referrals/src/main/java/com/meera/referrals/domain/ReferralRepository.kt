package com.meera.referrals.domain

import com.meera.referrals.domain.model.ReferralDataModel
import com.meera.referrals.domain.model.UserProfileModel

interface ReferralRepository {

    suspend fun getReferral(): ReferralDataModel

    suspend fun getVipReferral(): Boolean

    suspend fun checkReferralCode(code: String): Result<Boolean>

    suspend fun registerReferralCode(code: String): Boolean

    suspend fun getUserProfile(): UserProfileModel

    suspend fun getAppLinksUniquenameUrl(): String

    fun getAccountType(): Int

}
