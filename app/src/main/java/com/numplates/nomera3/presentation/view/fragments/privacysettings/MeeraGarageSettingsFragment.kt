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

class MeeraGarageSettingsFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.settings_garage)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            MeeraChangeProfileSettingsBottomSheetFragment.show(childFragmentManager)
            return
        }
        viewModel.setSetting(SettingsKeyEnum.SHOW_GARAGE.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.setSetting(
            key = SettingsKeyEnum.SHOW_GARAGE.key, value = SettingsUserTypeEnum.ALL.key, shouldUpdate = true
        )
    }

    override fun refreshCounters() = Unit

    override fun hasBlackWhiteLists(): Boolean = false

    companion object {
        const val GARAGE_SETTINGS_BOTTOM_DIALOG_TAG = "garageSettingsBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraGarageSettingsFragment {
            val instance = MeeraGarageSettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, GARAGE_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
