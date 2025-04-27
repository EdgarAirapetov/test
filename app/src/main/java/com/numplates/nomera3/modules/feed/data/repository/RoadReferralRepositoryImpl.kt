package com.numplates.nomera3.modules.feed.data.repository

import com.meera.core.base.exceptions.NetworkException
import com.meera.core.network.HTTP_CODE_SUCCESS
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.referrals.data.mapper.ReferralDataMapper
import com.meera.referrals.data.mapper.UserProfileMapper
import com.meera.referrals.data.model.ReferralDataDto
import com.meera.referrals.domain.model.ReferralDataModel
import com.meera.referrals.domain.model.UserProfileModel
import com.numplates.nomera3.modules.feed.data.api.RoadApi
import javax.inject.Inject

class RoadReferralRepositoryImpl @Inject constructor(
    private val roadApi: RoadApi,
    private val referralDataMapper: ReferralDataMapper,
    private val dataStore: DataStore,
    private val userProfileMapper: UserProfileMapper,
    private val appSettings: AppSettings
) : RoadReferralRepository {

    private var lastReferralInfo: ReferralDataDto? = null

    override suspend fun loadAndCacheReferral(): ReferralDataModel {
        val data = roadApi.getReferral().data
        if (data != null) {
            this.lastReferralInfo = data
            return referralDataMapper.mapReferralData(data)
        } else {
            throw NetworkException("Response get_referral_data contains null data")
        }
    }

    override fun getReferralInfo(): ReferralDataModel? {
        lastReferralInfo?.let { return referralDataMapper.mapReferralData(it) }
        return null
    }

    override suspend fun getVip(): Boolean {
        return roadApi.getVip().code() == HTTP_CODE_SUCCESS
    }

    override suspend fun getUserProfile(): UserProfileModel {
        val dbUserProfile = dataStore.userProfileDao().getUserProfile()
        return userProfileMapper.mapUserProfile(dbUserProfile)
    }

    override fun getAccountType(): Int {
        return appSettings.readAccountType()
    }
}
