package com.numplates.nomera3.modules.usersettings.domain.usecase

import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import javax.inject.Inject

class RestoreDefaultSettingsUseCase @Inject constructor(
    private val repository: PrivacyUserSettingsRepository
) {

    suspend fun invoke() {
        repository.restoreSettingsToDefault()
    }
}
