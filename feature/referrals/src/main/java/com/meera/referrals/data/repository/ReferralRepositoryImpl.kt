package com.meera.referrals.data.repository

import com.meera.core.base.exceptions.NetworkException
import com.meera.core.common.SHARE_PROFILE_BASE_URL
import com.meera.core.network.HTTP_CODE_SUCCESS
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.referrals.data.api.ReferralApi
import com.meera.referrals.data.mapper.ReferralDataMapper
import com.meera.referrals.data.mapper.UserProfileMapper
import com.meera.referrals.data.model.AppLinksPref
import com.meera.referrals.domain.ReferralRepository
import com.meera.referrals.domain.model.ReferralDataModel
import com.meera.referrals.domain.model.UserProfileModel
import javax.inject.Inject


class ReferralRepositoryImpl @Inject constructor(
    private val api: ReferralApi,
    private val dataStore: DataStore,
    private val appSettings: AppSettings,
    private val referralDataMapper: ReferralDataMapper,
    private val userProfileMapper: UserProfileMapper
) : ReferralRepository {

    override suspend fun getReferral(): ReferralDataModel {
        val data = api.getReferral().data
        if (data != null) {
            return referralDataMapper.mapReferralData(data)
        } else {
            throw NetworkException("Response get_referral_data contains null data")
        }
    }

    override suspend fun getVipReferral(): Boolean {
        return api.getVipReferral().code() == HTTP_CODE_SUCCESS
    }

    override suspend fun checkReferralCode(code: String): Result<Boolean> {
        val response = api.checkReferralCode(code)
        return if (response.err != null) {
            Result.failure(Exception(response.err?.message))
        } else {
            Result.success(true)
        }
    }

    override suspend fun registerReferralCode(code: String): Boolean {
        return api.registerReferralCode(code).code() == HTTP_CODE_SUCCESS
    }

    override suspend fun getUserProfile(): UserProfileModel {
        val dbUserProfile = dataStore.userProfileDao().getUserProfile()
        return userProfileMapper.mapUserProfile(dbUserProfile)
    }

    override suspend fun getAppLinksUniquenameUrl(): String {
        return if (appSettings.prefAppLinks(AppLinksPref::class.java) != null) {
            appSettings.prefAppLinks(AppLinksPref::class.java)?.uniqname ?: SHARE_PROFILE_BASE_URL
        } else {
            SHARE_PROFILE_BASE_URL
        }
    }

    override fun getAccountType(): Int = appSettings.readAccountType()
}
