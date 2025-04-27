package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum

class MeeraPrivacySettingsDataMapper {

    fun handleSettingsData(settings: List<PrivacySettingUiModel>): List<MeeraPrivacySettingsData> {
        return listOf(
            MeeraPrivacySettingsData.MeeraPrivacySettingsProfileModel(settings = getSettingsProfile(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsCommonModel(settings = getSettingsCommon(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsMapModel(settings = getSettingsMap(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsCommunicationModel(settings = getSettingsCommunication(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsBirthdayModel(settings = getSettingsBirthday(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsRoadModel(settings = getSettingsRoad(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsShakeModel(settings = getSettingsShake(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsModel(settings = getSettingsMoment(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsBlackListModel(settings = getSettingsBlackList(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsDefaultsModel(settings = getSettingsDefault())
        )
    }

    fun handleSettingsMomentData(settings: List<PrivacySettingUiModel>): List<MeeraPrivacySettingsData> {
        return listOf(
            MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsVisibility(settings = getSettingsMomentShowForFriends(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsAllowCommentModel(settings = getSettingsMomentAllowComment(settings)),
            MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsSaveToGalleryModel(settings = getSettingsMomentSaveToGallery(settings)),
        )
    }

    private fun getSettingsMap(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.SHOW_ON_MAP.key }
    }

    private fun getSettingsCommon(settings: List<PrivacySettingUiModel>): List<PrivacySettingUiModel> {
        return settings.filter { setting ->
            setting.key == SettingsKeyEnum.SHOW_ABOUT_ME.key ||
                setting.key == SettingsKeyEnum.SHOW_GARAGE.key ||
                setting.key == SettingsKeyEnum.SHOW_GIFTS.key ||
                setting.key == SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS.key
        }
    }

    private fun getSettingsProfile(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.CLOSED_PROFILE.key }
    }

    private fun getSettingsCommunication(settings: List<PrivacySettingUiModel>): List<PrivacySettingUiModel> {
        return settings.filter { setting ->
            setting.key == SettingsKeyEnum.SHOW_ONLINE.key ||
                setting.key == SettingsKeyEnum.WHO_CAN_CHAT.key ||
                setting.key == SettingsKeyEnum.HOW_CAN_CALL.key ||
                setting.key == SettingsKeyEnum.ALLOW_CONTACT_SYNC.key ||
                setting.key == SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key
        }
    }

    private fun getSettingsBirthday(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.REMIND_MY_BIRTHDAY.key }
    }

    private fun getSettingsRoad(settings: List<PrivacySettingUiModel>): List<PrivacySettingUiModel> {
        return settings.filter { setting ->
            setting.key == SettingsKeyEnum.HIDE_POSTS.key ||
                setting.key == SettingsKeyEnum.PROFANITY_ENABLED.key ||
                setting.key == SettingsKeyEnum.CREATE_AVATAR_POST.key ||
                setting.key == SettingsKeyEnum.SHOW_PERSONAL_ROAD.key
        }
    }

    private fun getSettingsBlackList(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.BLACKLIST.key }
    }

    private fun getSettingsMoment(settings: List<PrivacySettingUiModel>): List<PrivacySettingUiModel> {
        return settings.filter { setting ->
            setting.key == SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS.key ||
                setting.key == SettingsKeyEnum.MOMENTS_HIDE_FROM.key ||
                setting.key == SettingsKeyEnum.MOMENTS_ALLOW_COMMENT.key ||
                setting.key == SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY.key
        }
    }

    private fun getSettingsMomentShowForFriends(settings: List<PrivacySettingUiModel>): List<PrivacySettingUiModel> {
        return settings.filter { setting ->
            setting.key == SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS.key ||
                setting.key == SettingsKeyEnum.MOMENTS_HIDE_FROM.key ||
                setting.key == SettingsKeyEnum.MOMENTS_NOT_SHOW.key
        }
    }

    private fun getSettingsMomentAllowComment(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.MOMENTS_ALLOW_COMMENT.key }
    }

    private fun getSettingsMomentSaveToGallery(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY.key }
    }

    private fun getSettingsShake(settings: List<PrivacySettingUiModel>): PrivacySettingUiModel? {
        return settings.find { setting -> setting.key == SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key }
    }

    private fun getSettingsDefault(): List<PrivacySettingUiModel> {
        return emptyList()
    }
}
