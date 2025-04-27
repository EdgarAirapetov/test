package com.numplates.nomera3.modules.feed.data.repository

import com.meera.referrals.domain.model.ReferralDataModel
import com.meera.referrals.domain.model.UserProfileModel

interface RoadReferralRepository {

    suspend fun loadAndCacheReferral(): ReferralDataModel

    fun getReferralInfo(): ReferralDataModel?

    suspend fun getVip(): Boolean

    suspend fun getUserProfile(): UserProfileModel

    fun getAccountType(): Int

}
