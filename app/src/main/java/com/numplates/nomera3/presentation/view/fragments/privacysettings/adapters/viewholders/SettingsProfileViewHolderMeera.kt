package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsProfileBinding
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingsProfileViewHolderMeera(
    private val binding: MeeraItemTypeSettingsProfileBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsProfileModel
        data.settings?.value?.let {
            binding.cellPrivacyClosedProfile.setCellRightElementChecked(it.isTrue())
        }
        binding.cellPrivacyClosedProfile.setRightElementContainerClickable(false)
        binding.vCheckBox.setThrottledClickListener {
            adapterCallback.switchClosedProfile(
                SettingsKeyEnum.CLOSED_PROFILE.key, !binding.cellPrivacyClosedProfile.getCellRightElementChecked()
            )
        }
    }
}
