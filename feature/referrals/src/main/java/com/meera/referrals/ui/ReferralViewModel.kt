package com.meera.referrals.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.analytics.referrals.AmplitudePropertyColor
import com.meera.analytics.referrals.AmplitudePropertyDuration
import com.meera.analytics.referrals.AmplitudePropertyHaveVIPBefore
import com.meera.analytics.referrals.AmplitudePropertyWay
import com.meera.analytics.referrals.ReferralsAnalytics
import com.meera.core.base.enums.AccountTypeEnum
import com.meera.core.utils.getDateDDMMYYYY
import com.meera.referrals.domain.usecase.CheckReferralCodeUseCase
import com.meera.referrals.domain.usecase.GetAccountTypeUseCase
import com.meera.referrals.domain.usecase.GetAppLinksUniquenameUrl
import com.meera.referrals.domain.usecase.GetReferralUseCase
import com.meera.referrals.domain.usecase.GetUserProfile
import com.meera.referrals.domain.usecase.GetVipReferralUseCase
import com.meera.referrals.ui.mapper.ReferralDataUIMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ReferralViewModel @Inject constructor(
    private val getReferralUseCase: GetReferralUseCase,
    private val getVipReferralUseCase: GetVipReferralUseCase,
    private val checkReferralCodeUseCase: CheckReferralCodeUseCase,
    private val getUserProfile: GetUserProfile,
    private val getAppLinksUniquenameUrl: GetAppLinksUniquenameUrl,
    private val getAccountTypeUseCase: GetAccountTypeUseCase,
    private val referralsAnalytics: ReferralsAnalytics,
    private val mapper: ReferralDataUIMapper
) : ViewModel() {

    private var prefAccountType: Int = AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN.value

    val liveReferralDataViewEvent = MutableLiveData<ReferralViewEvent>()

    init {
        prefAccountType = getAccountTypeUseCase.invoke()
        getUserData()
        getReferral(isVipRequest = false, getDateDDMMYYYY(0L))
    }

    fun getReferral(isVipRequest: Boolean, date: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                val data = getReferralUseCase.invoke()
                val event = ReferralViewEvent.OnSuccessGetReferralData(mapper.mapReferralData(data))
                if (isVipRequest) {
                    prepareVipStatus(
                        vipUntilDate = date,
                        availableVips = event.refData.availableVips,
                        accountType = prefAccountType
                    )
                }
                liveReferralDataViewEvent.postValue(event)
            }.onFailure {
                liveReferralDataViewEvent.postValue(ReferralViewEvent.OnFailGetReferralData)
            }
        }
    }

    fun getVipReferral() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccessResult = getVipReferralUseCase.invoke()
                if (isSuccessResult) {
                    liveReferralDataViewEvent.postValue(ReferralViewEvent.OnSuccessGetVip)
                } else {
                    liveReferralDataViewEvent.postValue(ReferralViewEvent.OnFailGetVip)
                }
            } catch (e: Exception) {
                Timber.e(e)
                liveReferralDataViewEvent.postValue(ReferralViewEvent.OnFailGetVip)
            }
        }
    }

    fun checkReferralCode(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = checkReferralCodeUseCase.invoke(code)
                if (result.isSuccess) {
                    liveReferralDataViewEvent.postValue(ReferralViewEvent.OnSuccessCheckCode)
                } else {
                    liveReferralDataViewEvent.postValue(
                        ReferralViewEvent.OnFailCheckCode(result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                liveReferralDataViewEvent.postValue(ReferralViewEvent.OnFailCheckCode(null))
            }
        }
    }

    fun onSharedClicked() {
        viewModelScope.launch {
            kotlin.runCatching {
                onShareButtonClicked()
                val uniqueName = getUserProfile.invoke().uniqueName
                val url = getAppLinksUniquenameUrl.invoke()
                liveReferralDataViewEvent.postValue(
                    ReferralViewEvent.OnShareProfile(shareUrl = url + uniqueName)
                )
            }.onFailure { error ->
                Log.e(this@ReferralViewModel.javaClass.simpleName, error.toString())
            }
        }
    }

    fun onShareCodeCopied() {
        viewModelScope.launch {
            referralsAnalytics.onCodeCopied(userId = getUserProfile.invoke().userId)
        }
    }

    suspend fun getUserAccountTypeExpiration() = withContext(Dispatchers.IO) {
        return@withContext getUserProfile.invoke().accountTypeExpiration
    }

    private fun getUserData() {
        liveReferralDataViewEvent.value = ReferralViewEvent.OnGetAccountType(prefAccountType)
    }

    private fun prepareVipStatus(
        accountType: Int,
        vipUntilDate: String,
        availableVips: Int
    ) {
        repeat(availableVips) {
            pushVipStatus(
                accountType = accountType,
                vipUntilDate = vipUntilDate
            )
        }
    }

    private fun pushVipStatus(
        accountType: Int,
        vipUntilDate: String
    ) {
        referralsAnalytics.logBuyVipStatus(
            color = AmplitudePropertyColor.GOLD,
            duration = AmplitudePropertyDuration.MONTH,
            expirationDate = vipUntilDate,
            haveVipBefore = getHasVipBefore(accountType),
            way = AmplitudePropertyWay.FRIENDS_INVITATION
        )
    }

    private suspend fun onShareButtonClicked() {
        referralsAnalytics.onSendInvitation(userId = getUserProfile.invoke().userId)
    }

    private fun getHasVipBefore(accountType: Int): AmplitudePropertyHaveVIPBefore {
        return if (accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP.value
            || accountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM.value
        ) {
            AmplitudePropertyHaveVIPBefore.TRUE
        } else {
            AmplitudePropertyHaveVIPBefore.FALSE
        }
    }

}
