package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment

class MapSettingsFragment : BaseSettingsUserTypeFragment() {

    private val viewModel by viewModels<PrivacyNewViewModel>()
    private var origin: MapVisibilitySettingsOrigin = MapVisibilitySettingsOrigin.SETTINGS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        origin = arguments?.getSerializable(MapVisibilitySettingsOrigin.ARG) as? MapVisibilitySettingsOrigin
            ?: MapVisibilitySettingsOrigin.SETTINGS
        // Update counters after add / remove new users to exclusions
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            updateCounters(SettingsKeyEnum.SHOW_ON_MAP.key, settings)
        }
    }

    override fun screenTitle(): String = getString(R.string.settings_privacy_map_permission)

    override fun settingTypeTitle(): String = getString(R.string.settings_privacy_map_permission_description)

    override fun actionDescription(): String = String.empty()

    override fun hasBlackWhiteLists(): Boolean = true

    override fun actionTransitBlacklist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(
                    MapSettingsBlacklistFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(MapVisibilitySettingsOrigin.ARG, origin)
                )
            } else {
                add(
                    MapSettingsAddBlackListFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(MapVisibilitySettingsOrigin.ARG, origin)
                )
            }
        }
    }

    override fun actionTransitWhitelist(userCount: Int?) {
        userCount?.let { count ->
            if (count > 0) {
                add(
                    MapSettingsWhitelistFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(MapVisibilitySettingsOrigin.ARG, origin)
                )
            } else {
                add(
                    MapSettingsAddWhitelistFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(MapVisibilitySettingsOrigin.ARG, origin)
                )
            }
        }
    }

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        if (typeEnum.key == SettingsUserTypeEnum.ALL.key && closedProfile) {
            ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
            return
        }
        viewModel.logSettings(typeEnum, origin)
        viewModel.setSetting(SettingsKeyEnum.SHOW_ON_MAP.key, typeEnum.key)
    }

    override fun changeProfileSettingConfirmed() {
        super.changeProfileSettingConfirmed()
        viewModel.logSettings(SettingsUserTypeEnum.ALL, origin)
        viewModel.setSetting(
            key = SettingsKeyEnum.SHOW_ON_MAP.key, value = SettingsUserTypeEnum.ALL.key, shouldUpdate = true
        )
    }

    override fun refreshCounters() {
        viewModel.requestSettings()
    }
}
