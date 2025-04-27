package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.MeeraChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment

class MeeraPersonalFeedSettingsFragment : MeeraBaseSettingsUserTypeFragment() {
    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.personal_road)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun hasBlackWhiteLists(): Boolean = false

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            MeeraChangeProfileSettingsBottomSheetFragment.show(childFragmentManager)
            return
        }
        initRadioButtons(typeEnum.key, null, null)
        viewModel.setSetting(SettingsKeyEnum.SHOW_PERSONAL_ROAD.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.setSetting(
            key = SettingsKeyEnum.SHOW_PERSONAL_ROAD.key, value = SettingsUserTypeEnum.ALL.key, shouldUpdate = true
        )
    }

    override fun refreshCounters() = Unit

    companion object {
        const val PERSONAL_FEED_SETTINGS_BOTTOM_DIALOG_TAG = "personalFeedSettingsBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraPersonalFeedSettingsFragment {
            val instance = MeeraPersonalFeedSettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, PERSONAL_FEED_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
