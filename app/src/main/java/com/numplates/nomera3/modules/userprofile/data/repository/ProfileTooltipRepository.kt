package com.numplates.nomera3.modules.userprofile.data.repository

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

interface ProfileTooltipRepository {
    fun getUniqueNameHintShownTimes() : Int
    fun isShownTooltipSession() : Boolean
    fun setShownTooltipSession() : Boolean
    fun setUniqueNameShownTimes(times: Int)
    fun setCreateAvatarTooltipShown()
    fun getIsCreateUserInfoReferralToolTipShownTimes(): Int
    fun isCreateAvatarRegisterUserHintShown(): Boolean
    fun isCreateAvatarUserPersonalInfoHintShown(): Boolean
    fun getIsShownCreateUserInfoReferralTooltipSession(): Boolean
    fun setUserInfoReferralToolTipShowed(shownTimes: Int)
    fun getIsCreateAvatarUserInfoHintShown(): Boolean
}

class ProfileTooltipRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings
): ProfileTooltipRepository {
    override fun getUniqueNameHintShownTimes() =
        appSettings.readAboutUniqueNameHintShownTimes

    override fun isShownTooltipSession() =
        appSettings.isShownTooltipSession(AppSettings.IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES)

    override fun setShownTooltipSession() =
        appSettings.markTooltipAsShownSession(AppSettings.IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES)


    override fun setUniqueNameShownTimes(times: Int) {
        appSettings.readAboutUniqueNameHintShownTimes = times
    }

    override fun setCreateAvatarTooltipShown() {
        appSettings.isCreateAvatarUserInfoHintShown = true
        appSettings.markTooltipAsShownSession(AppSettings.CREATE_AVATAR_USER_INFO_HINT_SHOWN)
    }

    override fun isCreateAvatarRegisterUserHintShown(): Boolean = appSettings.isCreateAvatarRegisterUserHintShown
    override fun isCreateAvatarUserPersonalInfoHintShown(): Boolean =
        appSettings.isCreateAvatarUserPersonalInfoHintShown
    override fun getIsCreateUserInfoReferralToolTipShownTimes(): Int =
        appSettings.isCreateUserInfoReferralToolTipShownTimes
    override fun getIsShownCreateUserInfoReferralTooltipSession(): Boolean {
        return appSettings.shownTooltipsMap.contains(
            AppSettings.KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES
        )
    }
    override fun setUserInfoReferralToolTipShowed(shownTimes: Int) {
        appSettings.isCreateUserInfoReferralToolTipShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(AppSettings.KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES)
    }
    override fun getIsCreateAvatarUserInfoHintShown(): Boolean = appSettings.isCreateAvatarUserInfoHintShown

}
