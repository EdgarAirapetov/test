package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meera.core.extensions.empty
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment

class OnlineSettingsFragment : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner, Observer { settings ->
            updateCounters(SettingsKeyEnum.SHOW_ONLINE.key, settings)
        })
    }

    override fun screenTitle(): String = getString(R.string.settings_privacy_online_status)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = getString(R.string.settings_privacy_online_status_description)

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(OnlineSettingsBlacklistFragment(), Act.LIGHT_STATUSBAR)
            } else {
                add(OnlineSettingsAddBlacklistFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(OnlineSettingsWhitelistFragment(), Act.LIGHT_STATUSBAR)
            } else {
                add(OnlineSettingsAddWhitelistFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
            return
        }
        viewModel.setSetting(SettingsKeyEnum.SHOW_ONLINE.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.setSetting(
            key = SettingsKeyEnum.SHOW_ONLINE.key, value = SettingsUserTypeEnum.ALL.key, shouldUpdate = true
        )
    }

    override fun refreshCounters() {
        viewModel.requestSettings()
    }
}
