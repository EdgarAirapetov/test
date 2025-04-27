package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel

sealed interface MeeraPrivacySettingsData : RecyclerData<String, MeeraPrivacySettingsData> {
    data class MeeraPrivacySettingsProfileModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_PROFILE
    }

    data class MeeraPrivacySettingsCommonModel(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_COMMON
    }

    data class MeeraPrivacySettingsMapModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_MAP
    }

    data class MeeraPrivacySettingsCommunicationModel(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_COMMUNICATION
    }

    data class MeeraPrivacySettingsBirthdayModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_MY_BIRTHDAY
    }

    data class MeeraPrivacySettingsRoadModel(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_ROAD
    }

    data class MeeraPrivacySettingsShakeModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_SHAKE
    }

    data class MeeraPrivacySettingsMomentsModel(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_MOMENTS
    }

    data class MeeraPrivacySettingsMomentsVisibility(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = MeeraPrivacySettingsAdapter.SETTING_MOMENT_VISIBILITY
    }

    data class MeeraPrivacySettingsMomentsAllowCommentModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = MeeraPrivacySettingsAdapter.MOMENTS_ALLOW_COMMENT
    }

    data class MeeraPrivacySettingsMomentsSaveToGalleryModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = MeeraPrivacySettingsAdapter.SAVE_MOMENTS_TO_GALLERY
    }

    data class MeeraPrivacySettingsBlackListModel(
        val settings: PrivacySettingUiModel? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_BLACKLIST
    }

    data class MeeraPrivacySettingsDefaultsModel(
        val settings: List<PrivacySettingUiModel>? = null
    ) : MeeraPrivacySettingsData {
        override fun getItemId() = ""

        override fun contentTheSame(newItem: MeeraPrivacySettingsData) = this == newItem

        override fun itemViewType() = PrivacySettingsAdapter.SETTING_ITEM_TYPE_RESTORE_DEFAULTS
    }
}

