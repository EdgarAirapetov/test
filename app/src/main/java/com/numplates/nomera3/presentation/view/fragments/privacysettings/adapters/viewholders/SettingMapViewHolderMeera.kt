package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsMapBinding
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingMapViewHolderMeera(
    private val binding: MeeraItemTypeSettingsMapBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsMapModel
        var showOnlineValue: Int? = null
        var countBlacklist: Int? = null
        var countWhitelist: Int? = null

        if (data.settings?.key == SettingsKeyEnum.SHOW_ON_MAP.key) {
            val value = getUserExclusionTypeWithCount(
                value = data.settings.value,
                countBlacklist = data.settings.countBlacklist,
                countWhitelist = data.settings.countWhitelist
            )
            binding.cellPrivacyMap.setRightTextboxValue(value)
            showOnlineValue = data.settings.value
            countBlacklist = data.settings.countBlacklist
            countWhitelist = data.settings.countWhitelist
        }
        binding.cellPrivacyMap.setRightElementContainerClickable(false)
        binding.cellPrivacyMap.setThrottledClickListener {
            adapterCallback.clickMapPermissions(
                showOnlineValue, countBlacklist, countWhitelist
            )
        }
    }
}
