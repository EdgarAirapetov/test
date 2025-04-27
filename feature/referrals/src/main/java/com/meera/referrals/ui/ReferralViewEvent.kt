package com.meera.referrals.ui

import com.meera.referrals.ui.model.ReferralDataUIModel


sealed class ReferralViewEvent {
    class OnGetAccountType(val type: Int) : ReferralViewEvent()
    class OnSuccessGetReferralData(val refData: ReferralDataUIModel) : ReferralViewEvent()
    class OnShareProfile(val shareUrl: String) : ReferralViewEvent()
    object OnFailGetReferralData : ReferralViewEvent()
    object OnSuccessGetVip : ReferralViewEvent()
    object OnFailGetVip : ReferralViewEvent()
    object OnSuccessCheckCode : ReferralViewEvent()
    class OnFailCheckCode(val errorMessage: String?) : ReferralViewEvent()
    object OnSuccessRegisterWithCode : ReferralViewEvent()
    object OnFailRegisterWithCode : ReferralViewEvent()
}

