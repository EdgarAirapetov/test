package com.numplates.nomera3.presentation.view.fragments.privacysettings

import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment

class BirthdaySettingsFragment : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun screenTitle(): String = getString(R.string.settings_privacy_my_birthday)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = getString(R.string.settings_remind_birthday_description)

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun hasBlackWhiteLists(): Boolean = false

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
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
}
