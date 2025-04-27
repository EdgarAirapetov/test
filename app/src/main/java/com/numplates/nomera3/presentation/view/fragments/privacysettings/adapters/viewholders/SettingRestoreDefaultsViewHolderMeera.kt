package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsRestoreDefaultsBinding
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingRestoreDefaultsViewHolderMeera(
    private val binding: MeeraItemTypeSettingsRestoreDefaultsBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    init {
        binding.cellPrivacyRestoreDefaults.setRightElementContainerClickable(false)
        binding.cellPrivacyRestoreDefaults.setThrottledClickListener {
            adapterCallback.clickRestoreDefaultSettings()
        }
    }
    override fun bind(data: MeeraPrivacySettingsData) = Unit
}
