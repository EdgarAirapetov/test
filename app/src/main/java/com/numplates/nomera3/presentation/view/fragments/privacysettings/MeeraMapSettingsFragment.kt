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

class MeeraMapSettingsFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            updateCounters(SettingsKeyEnum.SHOW_ON_MAP.key, settings)
        }
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.settings_privacy_map_permission)

    override fun settingTypeTitle(): String = getString(R.string.settings_privacy_map_permission_description)

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraMapSettingsFragment_to_meeraMapSettingsBlacklistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraMapSettingsFragment_to_meeraMapSettingsAddBlackListFragment)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraMapSettingsFragment_to_meeraMapSettingsWhitelistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraMapSettingsFragment_to_meeraMapSettingsAddWhitelistFragment)
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        viewModel.setSetting(SettingsKeyEnum.SHOW_ON_MAP.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() = Unit

    override fun refreshCounters() {
        viewModel.requestSettings()
    }

    companion object {
        const val MAP_SETTINGS_BOTTOM_DIALOG_TAG = "mapSettingsBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraMapSettingsFragment {
            val instance = MeeraMapSettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, MAP_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
