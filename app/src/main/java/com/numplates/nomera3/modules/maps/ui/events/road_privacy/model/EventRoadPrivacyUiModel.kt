package com.numplates.nomera3.modules.maps.ui.events.road_privacy.model

import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum

data class EventRoadPrivacyUiModel(
    val roadPrivacySettingValue: SettingsUserTypeEnum,
    val isPublishEnabled: Boolean
)
