package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingBlacklistViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingCommonViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingCommunicationViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingMapViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingMomentsViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingMyBirthdayViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingRestoreDefaultsViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingRoadViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.SettingsProfileViewHolderMeera
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.ShakeSettingsViewHolderMeera

class MeeraPrivacySettingsAdapter(
    private val adapterCallback: IPrivacySettingsInteractor
) : BaseAsyncAdapter<String, MeeraPrivacySettingsData>() {

    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<MeeraPrivacySettingsData, *> {
        return when (viewType) {
            SETTING_ITEM_TYPE_MAP -> SettingMapViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_COMMON -> SettingCommonViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_PROFILE -> SettingsProfileViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_COMMUNICATION -> SettingCommunicationViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_MY_BIRTHDAY -> SettingMyBirthdayViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_ROAD -> SettingRoadViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_BLACKLIST -> SettingBlacklistViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_RESTORE_DEFAULTS -> SettingRestoreDefaultsViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_MOMENTS -> SettingMomentsViewHolderMeera(parent.toBinding(), adapterCallback)
            SETTING_ITEM_TYPE_SHAKE -> ShakeSettingsViewHolderMeera(parent.toBinding(), adapterCallback)
            else -> throw IllegalArgumentException("No such view type in adapter")
        }
    }

    fun getCurrentCollection(): List<MeeraPrivacySettingsData>{
        val oldListFormat = mutableListOf<MeeraPrivacySettingsData>()
        currentList.forEach {
            oldListFormat.add( it as  MeeraPrivacySettingsData)
        }
        return oldListFormat
    }


    interface IPrivacySettingsInteractor {

        fun switchGender(key: String, isEnabled: Boolean)
        fun switchAge(key: String, isEnabled: Boolean)
        fun switchAntiObscene(key: String, isEnabled: Boolean)
        fun switchNewAvatarPost(key: String, isEnabled: Boolean)
        fun switchShake(key: String, isEnabled: Boolean)
        fun switchClosedProfile(key: String, isEnabled: Boolean)
        fun switchContactSync(key: String, isEnabled: Boolean)
        fun switchShareScreenshot(key: String, isEnabled: Boolean)

        fun clickBirthdayDetails(value: Int?)
        fun clickOnlineStatus(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickHideRoadPosts(count: Int?)
        fun clickMapPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickCallPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickBlacklistUsers(count: Int?)
        fun clickAboutMePrivacy(value: Int?)
        fun clickGaragePrivacy(value: Int?)
        fun clickGiftPrivacy(value: Int?)
        fun clickPersonalFeedPrivacy(value: Int?)
        fun clickPersonalMessages(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun onFriendsAndFollowersClicked(value: Int?)
        fun clickRestoreDefaultSettings()
        fun clickMomentSettings()
    }

    companion object {
        const val SETTING_ITEM_TYPE_PROFILE = -1
        const val SETTING_ITEM_TYPE_COMMON = 0
        const val SETTING_ITEM_TYPE_ROAD = 1
        const val SETTING_ITEM_TYPE_MAP = 2
        const val SETTING_ITEM_TYPE_CALLS = 3
        const val SETTING_ITEM_TYPE_STORY = 4
        const val SETTING_ITEM_TYPE_BLACKLIST = 5
        const val SETTING_ITEM_TYPE_MY_BIRTHDAY = 6
        const val SETTING_ITEM_TYPE_COMMUNICATION = 7
        const val SETTING_ITEM_TYPE_RESTORE_DEFAULTS = 8
        const val SETTING_ITEM_TYPE_MOMENTS = 9
        const val SETTING_ITEM_TYPE_SHAKE = 10
        const val SETTING_MOMENT_VISIBILITY = 11
        const val MOMENTS_ALLOW_COMMENT = 12
        const val SAVE_MOMENTS_TO_GALLERY = 13
    }

}
