package com.numplates.nomera3.presentation.view.fragments.meerasettings.data

import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.MeeraSettingsRepository
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel
import timber.log.Timber
import javax.inject.Inject

class MeeraSettingsRepositoryImpl @Inject constructor(
    private val api: SettingsApi,
    private val mapper: MeeraSettingsDtoMapper
) : MeeraSettingsRepository {
    override suspend fun updatePushSettings(userId: Long, settings: PushSettingsModel): Boolean {
        val data = mapper.mapPushSettingsModelToDto(settings)
        return api.updatePushSettings(data, userId).data != null
    }

    override suspend fun getPushSettings(userId: Long): PushSettingsModel {
        val settings = api.getPushSettings(userId).data
        Timber.d(" settings ${settings}")
        return mapper.mapPushSettingsDtoToModel(settings)
    }
}
