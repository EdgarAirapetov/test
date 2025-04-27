package com.numplates.nomera3.modules.usersettings.domain.usecase

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: PrivacyUserSettingsRepository
) {
    suspend fun invoke(): List<PrivacySettingModel> {
        return repository.getUserPrivacySettings()
    }
}
