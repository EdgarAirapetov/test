package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsRoadBinding
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingRoadViewHolderMeera(
    private val binding: MeeraItemTypeSettingsRoadBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsRoadModel
        var count: Int? = 0
        // Set all setting
        data.settings?.forEach { setting ->
            when (setting.key) {
                SettingsKeyEnum.HIDE_POSTS.key -> {
                    count = setting.countBlacklist
                    // show hidden post container if needed
                    count?.let { hiddenPostCount: Int ->
                        if (hiddenPostCount > 0) {
                            binding.cellPrivacyRoadHiddenPost.visible()
                            binding.cellPrivacyRoadHiddenPost.setRightTextboxValue((hiddenPostCount).toString())
                        } else {
                            binding.cellPrivacyRoadHiddenPost.gone()
                        }
                    }
                }

                SettingsKeyEnum.PROFANITY_ENABLED.key -> {
                    setting.value?.let {
                        binding.cellPrivacyAbscene.setCellRightElementChecked(it.isTrue())
                    }
                    binding.cellPrivacyAbscene.setCellRightElementClickable(false)
                }

                SettingsKeyEnum.CREATE_AVATAR_POST.key -> {
                    binding.cellPrivacyNewAvatarPost.visible()
                    binding.cellPrivacyNewAvatarPost.setCellRightElementClickable(false)
                    val settingValue =
                        if (setting.value == CreateAvatarPostEnum.PRIVATE_ROAD.state) CreateAvatarPostEnum.MAIN_ROAD.state else setting.value
                    settingValue?.let {
                        binding.cellPrivacyNewAvatarPost.setCellRightElementChecked(it.isTrue())
                    }
                }

                SettingsKeyEnum.SHOW_PERSONAL_ROAD.key -> {
                    val roadValue = getUserExclusionType(setting.value)
                    binding.cellPrivacyPersonalRoad.setRightTextboxValue(roadValue)
                    binding.cellPrivacyPersonalRoad.setRightElementContainerClickable(false)
                    binding.cellPrivacyPersonalRoad.setThrottledClickListener {
                        adapterCallback.clickPersonalFeedPrivacy(setting.value)
                    }
                }
            }
        }
        initClickListener(count)
    }

    private fun initClickListener(count: Int?){
        binding.cellPrivacyNewAvatarPost.setCellRightElementClickable(false)
        binding.cellPrivacyNewAvatarPost.cellRightIconClickListener = {
            adapterCallback.switchNewAvatarPost(
                SettingsKeyEnum.CREATE_AVATAR_POST.key,
                binding.cellPrivacyNewAvatarPost.getCellRightElementChecked().not()
            )
        }

        binding.cellPrivacyAbscene.setCellRightElementClickable(false)
        binding.cellPrivacyAbscene.cellRightIconClickListener = {
            adapterCallback.switchAntiObscene(
                SettingsKeyEnum.PROFANITY_ENABLED.key, binding.cellPrivacyAbscene.getCellRightElementChecked().not()
            )
        }

        binding.cellPrivacyRoadHiddenPost.setRightElementContainerClickable(false)
        binding.cellPrivacyRoadHiddenPost.setThrottledClickListener {
            adapterCallback.clickHideRoadPosts(count)
        }
    }
}
