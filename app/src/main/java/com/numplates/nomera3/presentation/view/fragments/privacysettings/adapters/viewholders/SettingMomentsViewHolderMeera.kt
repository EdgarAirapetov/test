package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsMomentsBinding
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingMomentsViewHolderMeera(
    private val binding: MeeraItemTypeSettingsMomentsBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsModel
        binding.cellPrivacyMoments.setRightElementContainerClickable(false)
        binding.cellPrivacyMoments.setThrottledClickListener {
            adapterCallback.clickMomentSettings()
        }
    }
}
