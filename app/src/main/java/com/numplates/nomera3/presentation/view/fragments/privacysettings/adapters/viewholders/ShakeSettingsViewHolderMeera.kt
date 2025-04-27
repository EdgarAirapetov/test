package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsShakeBinding
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class ShakeSettingsViewHolderMeera(
    private val binding: MeeraItemTypeSettingsShakeBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsShakeModel
        binding.cellPrivacyShake.apply {
            setCellRightElementChecked(data.settings?.value.toBoolean())
            setCellRightElementClickable(false)
            cellRightIconClickListener = {
                adapterCallback.switchShake(
                    key = SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key,
                    isEnabled = getCellRightElementChecked().not()
                )
            }
        }
    }
}
