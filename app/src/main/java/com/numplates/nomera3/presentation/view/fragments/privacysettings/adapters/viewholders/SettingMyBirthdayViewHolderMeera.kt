package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsBirhtdayBinding
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingMyBirthdayViewHolderMeera(
    private val binding: MeeraItemTypeSettingsBirhtdayBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsBirthdayModel
        val valueString = getUserExclusionTypeWithCount(
            value = data.settings?.value,
            countBlacklist = null,
            countWhitelist = null,
        )
        binding.cellPrivacyBirthday.apply {
            setRightTextboxValue(valueString)
            setRightElementContainerClickable(false)
            setThrottledClickListener { adapterCallback.clickBirthdayDetails(data.settings?.value) }
        }
    }
}
