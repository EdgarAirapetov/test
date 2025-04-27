package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsCommonBinding
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingCommonViewHolderMeera(
    private val binding: MeeraItemTypeSettingsCommonBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsCommonModel

        data.settings?.forEach { setting ->
            when (setting.key) {
                SettingsKeyEnum.SHOW_ABOUT_ME.key -> {
                    binding.cellPrivacyAboutMe.apply {
                        setRightTextboxValue(getUserExclusionType(setting.value))
                        setRightElementContainerClickable(false)
                        setThrottledClickListener {
                            adapterCallback.clickAboutMePrivacy(setting.value)
                        }
                    }
                }

                SettingsKeyEnum.SHOW_GARAGE.key -> {
                    binding.cellPrivacyGarage.apply {
                        setRightTextboxValue(getUserExclusionType(setting.value))
                        setRightElementContainerClickable(false)
                        setThrottledClickListener {
                            adapterCallback.clickGaragePrivacy(setting.value)
                        }
                    }
                }

                SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS.key -> {
                    binding.cellPrivacyFriendsFollowers.apply {
                        setRightTextboxValue(getUserExclusionType(setting.value))
                        setRightElementContainerClickable(false)
                        setThrottledClickListener {
                            adapterCallback.onFriendsAndFollowersClicked(setting.value)
                        }
                    }
                }
            }
        }
    }
}
