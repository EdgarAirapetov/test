package com.numplates.nomera3.modules.bump.domain.usecase

import com.meera.core.preferences.datastore.Preference
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import javax.inject.Inject

class ObserveShakePrivacySetting @Inject constructor(
    private val repository: ShakeRepository
) {
    suspend fun invoke(): Preference<Boolean> = repository.observeShakePrivacySettingChanged()
}
