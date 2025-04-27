package com.numplates.nomera3.modules.usersettings.domain.usecase

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import javax.inject.Inject

class GetLocalSettingByKey @Inject constructor(
    private val repository: PrivacyUserSettingsRepository
) {

    suspend fun invoke(key: String): PrivacySettingModel {
        return repository.getUserSettingByKey(key)
    }
}
