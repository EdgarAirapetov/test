package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemTypeSettingsCommunicationBinding
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

class SettingCommunicationViewHolderMeera(
    private val binding: MeeraItemTypeSettingsCommunicationBinding,
    private val adapterCallback: MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor
) : MeeraBaseSettingsViewHolder(binding) {

    private var showOnlineValue: Int? = null
    private var countBlacklist: Int? = null
    private var countWhitelist: Int? = null

    private var showCallOnlineValue: Int? = null
    private var countCallBlacklist: Int? = null
    private var countCallWhitelist: Int? = null

    private var showChatMessageValue: Int? = null
    private var countChatBlackList: Int? = null
    private var countChatWhiteList: Int? = null

    override fun bind(data: MeeraPrivacySettingsData) {
        data as MeeraPrivacySettingsData.MeeraPrivacySettingsCommunicationModel

        data.settings?.forEach { setting ->
            when (setting.key) {
                SettingsKeyEnum.SHOW_ONLINE.key -> {
                    val onlineValue =
                        getUserExclusionTypeWithCount(setting.value, setting.countBlacklist, setting.countWhitelist)
                    binding.cellPrivacyOnline.setRightTextboxValue(onlineValue)
                    showOnlineValue = setting.value
                    countBlacklist = setting.countBlacklist
                    countWhitelist = setting.countWhitelist
                }

                SettingsKeyEnum.HOW_CAN_CALL.key -> {
                    val callValue =
                        getUserExclusionTypeWithCount(setting.value, setting.countBlacklist, setting.countWhitelist)
                    binding.cellPrivacyCalls.setRightTextboxValue(callValue)
                    showCallOnlineValue = setting.value
                    countCallBlacklist = setting.countBlacklist
                    countCallWhitelist = setting.countWhitelist
                }

                SettingsKeyEnum.WHO_CAN_CHAT.key -> {
                    val messageValue = getUserExclusionTypeWithCount(
                        setting.value, setting.countBlacklist, setting.countWhitelist
                    )
                    binding.cellPrivacyPersonalMessages.setRightTextboxValue(messageValue)
                    showChatMessageValue = setting.value
                    countChatBlackList = setting.countBlacklist
                    countChatWhiteList = setting.countWhitelist
                }

                SettingsKeyEnum.ALLOW_CONTACT_SYNC.key -> {
                    setting.value?.let {
                        binding.cellPrivacySyncContacts.setCellRightElementChecked(it.isTrue())
                    }
                }

                SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key -> {
                    setting.value?.let {
                        binding.cellPrivacyShareScreenshot.setCellRightElementChecked(it.isTrue())
                    }
                }
            }
        }
        initClickListener()
    }

    private fun initClickListener() {
        binding.cellPrivacyCalls.setRightElementContainerClickable(false)
        binding.cellPrivacyCalls.setThrottledClickListener {
            adapterCallback.clickCallPermissions(showCallOnlineValue, countCallBlacklist, countCallWhitelist)
        }

        binding.cellPrivacyOnline.setRightElementContainerClickable(false)
        binding.cellPrivacyOnline.setThrottledClickListener {
            adapterCallback.clickOnlineStatus(showOnlineValue, countBlacklist, countWhitelist)
        }

        binding.cellPrivacyPersonalMessages.setRightElementContainerClickable(false)
        binding.cellPrivacyPersonalMessages.setThrottledClickListener {
            adapterCallback.clickPersonalMessages(showChatMessageValue, countChatBlackList, countChatWhiteList)
        }

        binding.cellPrivacySyncContacts.setCellRightElementClickable(false)
        binding.cellPrivacySyncContacts.cellRightIconClickListener = {
            adapterCallback.switchContactSync(
                key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                isEnabled = binding.cellPrivacySyncContacts.getCellRightElementChecked().not()
            )
        }

        binding.cellPrivacyShareScreenshot.setCellRightElementClickable(false)
        binding.cellPrivacyShareScreenshot.cellRightIconClickListener = {
            adapterCallback.switchShareScreenshot(
                key = SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key,
                isEnabled = binding.cellPrivacyShareScreenshot.getCellRightElementChecked().not()
            )
        }
    }
}
