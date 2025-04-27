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

class MeeraBirthdaySettingsFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.settings_privacy_my_birthday)

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
        viewModel.setSetting(SettingsKeyEnum.REMIND_MY_BIRTHDAY.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.setSetting(
            key = SettingsKeyEnum.REMIND_MY_BIRTHDAY.key, value = SettingsUserTypeEnum.ALL.key, shouldUpdate = true
        )
    }

    override fun refreshCounters() {
        viewModel.requestSettings()
    }

    companion object {
        const val BIRTHDAY_SETTINGS_BOTTOM_DIALOG_TAG = "birthdaySettingsBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraBirthdaySettingsFragment {
            val instance = MeeraBirthdaySettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, BIRTHDAY_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
