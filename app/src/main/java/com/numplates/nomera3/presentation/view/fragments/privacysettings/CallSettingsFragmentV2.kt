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
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment

class CallSettingsFragmentV2 : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner, Observer { settings ->
            updateCounters(SettingsKeyEnum.HOW_CAN_CALL.key, settings)
        })
    }

    override fun screenTitle(): String = getString(R.string.settings_who_call_me)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(CallSettingsBlacklistFragment(), Act.LIGHT_STATUSBAR)
            } else {
                add(CallSettingsAddBlacklistFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(CallSettingsWhitelistFragment(), Act.LIGHT_STATUSBAR)
            } else {
                add(CallSettingsAddWhitelistFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        viewModel.setSetting(SettingsKeyEnum.HOW_CAN_CALL.key, typeEnum.key)
    }

    override fun refreshCounters() {
        viewModel.requestSettings()
    }

    override fun changeProfileSettingConfirmed() = Unit

}
