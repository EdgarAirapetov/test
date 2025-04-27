package com.numplates.nomera3.modules.usersettings.domain.usecase

import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.Job
import javax.inject.Inject

class SetSettingsUseCase @Inject constructor(
    private val settingsRepository: PrivacyUserSettingsRepository,
) {

    fun invoke(params: SettingsParams) : Job {
        return settingsRepository.setUserPersonalPrivacySetting(params.key, params.value)
    }
}

/**
 * Helper params class to interact with settings repository
 */
sealed class SettingsParams(val key: String, val value: Int) {

    /**
     * Setting params class which force to use in app settings classes: [SettingsKeyEnum] and
     * [SettingsUserTypeEnum]. It should helps to reduce error prone in the code base.
     */
    class PrivacySettingsParams(key: SettingsKeyEnum, value: SettingsUserTypeEnum) :
        SettingsParams(key.key, value.key)

    /**
     * Simple settings param implementation which allows to use any key / value pair.
     * Please consider using some of specific implementations and use this one mostly
     * for migration from legacy code.
     */
    class CommonSettingsParams(key: String, value: Int) : SettingsParams(key, value)
}
