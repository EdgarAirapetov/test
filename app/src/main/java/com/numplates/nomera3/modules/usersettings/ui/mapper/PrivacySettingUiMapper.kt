package com.numplates.nomera3.modules.usersettings.ui.mapper

import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import javax.inject.Inject

class PrivacySettingUiMapper @Inject constructor(private val featureTogglesContainer: FeatureTogglesContainer) {

    fun mapModelToUi(source: PrivacySettingModel): PrivacySettingUiModel? {
        if (featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled) {
            return when (source.key) {
                SettingsKeyEnum.SHOW_BIRTHDAY.key, SettingsKeyEnum.SHOW_GENDER.key -> null

                else -> PrivacySettingUiModel(
                    key = source.key,
                    value = source.value,
                    countBlacklist = source.countBlacklist,
                    countWhitelist = source.countWhitelist,
                )
            }
        }
        return PrivacySettingUiModel(
            key = source.key,
            value = source.value,
            countBlacklist = source.countBlacklist,
            countWhitelist = source.countWhitelist,
        )
    }
}
