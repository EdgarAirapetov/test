package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment

class PersonalMessagesFragment : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            updateCounters(SettingsKeyEnum.WHO_CAN_CHAT.key, settings)
        }
    }

    override fun screenTitle(): String = getString(R.string.private_setting_communication_messages)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        checkAppRedesigned (
            isRedesigned = {},
            isNotRedesigned = {
                userCount?.let { count ->
                    if (count > 0) {
                        add(PersonalMessagesBlackListFragment(), Act.LIGHT_STATUSBAR)
                    } else {
                        add(PersonalMessageAddBlackListFragment(), Act.LIGHT_STATUSBAR)
                    }
                }
            }
        )
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                userCount?.let { count ->
                    if (count > 0) {
                        add(PersonalMessageWhiteListFragment(), Act.LIGHT_STATUSBAR)
                    } else {
                        add(PersonalMessageAddWhiteListFragment(), Act.LIGHT_STATUSBAR)
                    }
                }
            }
        )
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        viewModel.setSetting(SettingsKeyEnum.WHO_CAN_CHAT.key, typeEnum.key)
    }

    override fun refreshCounters() = viewModel.requestSettings()

    override fun changeProfileSettingConfirmed() = Unit
}
