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
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.MeeraChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment

class MeeraOnlineSettingsFragment : MeeraBaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner, Observer { settings ->
            updateCounters(SettingsKeyEnum.SHOW_ONLINE.key, settings)
        })
        contentBinding?.tvExceptionDescription?.gone()
        contentBinding?.bellowActionDescription?.gone()
    }

    override fun screenTitle(): String = getString(R.string.settings_privacy_online_status)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraOnlineSettingsFragment_to_meeraOnlineSettingsBlacklistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraOnlineSettingsFragment_to_meeraOnlineSettingsAddBlacklistFragment)
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraOnlineSettingsFragment_to_meeraOnlineSettingsWhitelistFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraOnlineSettingsFragment_to_meeraOnlineSettingsAddWhitelistFragment)
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            MeeraChangeProfileSettingsBottomSheetFragment.show(childFragmentManager)
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

    companion object {
        const val ONLINE_SETTINGS_BOTTOM_DIALOG_TAG = "onlineSettingsBottomDialogTag"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraOnlineSettingsFragment {
            val instance = MeeraOnlineSettingsFragment()
            instance.arguments = args
            instance.show(fragmentManager, ONLINE_SETTINGS_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
