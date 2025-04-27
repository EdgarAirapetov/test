package com.numplates.nomera3.presentation.view.fragments.privacysettings


import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.ShowFriendsSubscribersPrivacyViewModel


class ShowFriendsSubscribersPrivacyFragment : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<ShowFriendsSubscribersPrivacyViewModel> {
        App.component.getViewModelFactory()
    }

    override fun screenTitle(): String = getString(R.string.friends_and_followers)

    override fun settingTypeTitle() = String.empty()

    override fun actionDescription() = getString(R.string.select_friends_followers_privacy_description)

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
            return
        }
        viewModel.setSetting(SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS, typeEnum, false)
        viewModel.setPreferencesPrivacy(typeEnum)
        viewModel.logAmplitudeFriendsPrivacySelected(typeEnum)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.setSetting(SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS, SettingsUserTypeEnum.ALL, true)
        viewModel.setPreferencesPrivacy(SettingsUserTypeEnum.ALL)
        viewModel.logAmplitudeFriendsPrivacySelected(SettingsUserTypeEnum.ALL)
    }

    override fun hasBlackWhiteLists(): Boolean = false

    override fun refreshCounters() = Unit
}
