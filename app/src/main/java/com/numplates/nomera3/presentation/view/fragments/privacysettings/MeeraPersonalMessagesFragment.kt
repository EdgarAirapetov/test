package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment

class MeeraPersonalMessagesFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            updateCounters(SettingsKeyEnum.WHO_CAN_CHAT.key, settings)
        }
        contentBinding?.tvExceptionDescription?.gone()
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.private_setting_communication_messages)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraPersonalMessagesFragment_to_meeraPersonalMessagesBlackListFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraPersonalMessagesFragment_to_meeraPersonalMessageAddBlackListFragment)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraPersonalMessagesFragment_to_meeraPersonalMessageWhiteListFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraPersonalMessagesFragment_to_meeraPersonalMessageAddWhiteListFragment)
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        viewModel.setSetting(SettingsKeyEnum.WHO_CAN_CHAT.key, typeEnum.key)
    }

    override fun refreshCounters() = viewModel.requestSettings()

    override fun changeProfileSettingConfirmed() = Unit

    companion object {
        const val PERSONAL_MESSAGES_BOTTOM_DIALOG_TAG = "personalMessagesBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraPersonalMessagesFragment {
            val instance = MeeraPersonalMessagesFragment()
            instance.arguments = args
            instance.show(fragmentManager, PERSONAL_MESSAGES_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
