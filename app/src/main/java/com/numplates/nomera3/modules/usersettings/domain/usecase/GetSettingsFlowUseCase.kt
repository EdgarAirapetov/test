package com.numplates.nomera3.modules.usersettings.domain.usecase

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsFlowUseCase @Inject constructor(
    private val repository: PrivacyUserSettingsRepository,
) {

    fun invoke(): Flow<Result<List<PrivacySettingModel>>> {
        return repository.getUserPrivacySettingsFlow()
    }
}
