package com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.usecase

import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.MeeraSettingsRepository
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel
import javax.inject.Inject

class MeeraSetPushSettingUseCase @Inject constructor(
    private val getUserIdUseCase: GetUserUidUseCase,
    private val repository: MeeraSettingsRepository
) {
    suspend fun invoke(settings: PushSettingsModel) =
        repository.updatePushSettings(getUserIdUseCase.invoke(), settings)
}
