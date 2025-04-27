package com.numplates.nomera3.presentation.view.fragments.meerasettings.domain

import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel

interface MeeraSettingsRepository {

    suspend fun updatePushSettings(userId: Long, settings: PushSettingsModel): Boolean
    suspend fun getPushSettings(userId: Long): PushSettingsModel
}
