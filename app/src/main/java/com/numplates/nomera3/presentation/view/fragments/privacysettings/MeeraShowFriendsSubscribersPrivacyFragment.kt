package com.numplates.nomera3.presentation.view.fragments.privacysettings


import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.MeeraChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.ShowFriendsSubscribersPrivacyViewModel


class MeeraShowFriendsSubscribersPrivacyFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<ShowFriendsSubscribersPrivacyViewModel> {
        App.component.getViewModelFactory()
    }

    override fun screenTitle(): String = getString(R.string.friends_and_followers)

    override fun settingTypeTitle() = String.empty()

    override fun actionDescription() = String.empty()

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.tvExceptionDescription?.gone()
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            MeeraChangeProfileSettingsBottomSheetFragment.show(childFragmentManager)
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

    companion object {
        const val SHOW_FRIENDS_SUBSCRIBERS_PRIVACY_BOTTOM_DIALOG_TAG =
            "SHOW_FRIENDS_SUBSCRIBERS_PRIVACY_BOTTOM_DIALOG_TAG"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraShowFriendsSubscribersPrivacyFragment {
            val instance = MeeraShowFriendsSubscribersPrivacyFragment()
            instance.arguments = args
            instance.show(fragmentManager, SHOW_FRIENDS_SUBSCRIBERS_PRIVACY_BOTTOM_DIALOG_TAG)
            return instance
        }
    }

}
