package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsBlacklistBinding
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingBlacklistViewHolderMeera(
    private val binding: MeeraItemTypeSettingsBlacklistBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =  UiKitNavigationBarViewVisibilityState.GONE
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsBlackListModel
        val value = getCountBlacklist(data.settings?.countBlacklist)
        binding.cellPrivacyBlacklist.apply {
            if (value.toInt() > 0) {
                setRightTextboxValue(value)
            } else {
                setRightTextboxValue(binding.root.resources.getString(R.string.general_add))
            }
            setRightElementContainerClickable(false)
            setThrottledClickListener {
                adapterCallback.clickBlacklistUsers(data.settings?.countBlacklist)
            }
        }
    }
}
