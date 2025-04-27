package com.numplates.nomera3.modules.maps.ui.events.road_privacy.mapper

import com.numplates.nomera3.modules.maps.ui.events.road_privacy.model.EventRoadPrivacyUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class EventRoadPrivacyUiMapper @Inject constructor() {

    fun mapUiModel(roadPrivacySettingValue: SettingsUserTypeEnum): EventRoadPrivacyUiModel = EventRoadPrivacyUiModel(
        roadPrivacySettingValue = roadPrivacySettingValue,
        isPublishEnabled = roadPrivacySettingValue == SettingsUserTypeEnum.ALL
    )
}
