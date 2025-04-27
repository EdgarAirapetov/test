package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import javax.inject.Inject

class PushSetPrivacySettingsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getSettingsUseCase: GetSettingsUseCase
) {
    suspend fun invoke(key: String, model: CustomRowSelector.CustomRowSelectorModel) {
        userRepository.pushSetPrivacySettings(key, model)
        getSettingsUseCase.invoke()
    }
}
