package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.flow.Flow

interface MapSettingsRepository {

    fun getMapSettings(): MapSettingsModel

    fun setMapSettings(mapSettingsModel: MapSettingsModel)

    fun observeUserVisibilityOnMapSetting(): Flow<SettingsUserTypeEnum>

    fun setUserVisibilityOnMapSetting(typeEnum: SettingsUserTypeEnum)

    suspend fun getUserVisibilityOnMapSetting(): SettingsUserTypeEnum
}
