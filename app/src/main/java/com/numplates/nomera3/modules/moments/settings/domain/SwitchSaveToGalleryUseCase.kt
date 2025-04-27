package com.numplates.nomera3.modules.moments.settings.domain

import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class SwitchSaveToGalleryUseCase @Inject constructor(
    private val repository: PrivacyUserSettingsRepository
) {

    private val settingKey = SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY.key

    suspend fun invoke() {
        val settings = repository.getLocalUserPrivacySettings()
        val saveToGallery = settings.find { it.key == settingKey }
        repository.setUserPersonalPrivacySetting(
            settingKey,
            toggleIntKey(saveToGallery?.value)
        )
    }

    private fun toggleIntKey(key: Int?): Int {
        return if (key == SettingsUserTypeEnum.NOBODY.key) {
            SettingsUserTypeEnum.ALL
        } else {
            SettingsUserTypeEnum.NOBODY
        }.key
    }
}
