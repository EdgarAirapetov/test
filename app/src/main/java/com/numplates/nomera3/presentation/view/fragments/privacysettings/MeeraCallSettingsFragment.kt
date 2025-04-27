package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment

class MeeraCallSettingsFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner, Observer { settings ->
            updateCounters(SettingsKeyEnum.HOW_CAN_CALL.key, settings)
        })
        contentBinding?.tvExceptionDescription?.gone()
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.settings_who_call_me)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraCallSettingsFragment_to_meeraCallSettingsBlacklistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraCallSettingsFragment_to_meeraCallSettingsAddBlacklistFragment)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraCallSettingsFragment_to_meeraCallSettingsWhitelistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraCallSettingsFragment_to_meeraCallSettingsAddWhitelistFragment)
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

    companion object {
        const val CALL_SETTINGS_BOTTOM_DIALOG_TAG = "callSettingsBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraCallSettingsFragment {
            val instance = MeeraCallSettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, CALL_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
