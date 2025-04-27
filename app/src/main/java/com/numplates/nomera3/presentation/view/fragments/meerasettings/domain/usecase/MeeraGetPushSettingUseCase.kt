package com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.usecase

import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.MeeraSettingsRepository
import javax.inject.Inject

class MeeraGetPushSettingUseCase @Inject constructor(
    private val getUserIdUseCase: GetUserUidUseCase,
    private val repository: MeeraSettingsRepository
) {
    suspend fun invoke() = repository.getPushSettings(getUserIdUseCase.invoke())
}
