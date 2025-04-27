package com.numplates.nomera3.modules.moments.settings.util

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum

fun List<PrivacySettingModel>.getSetting(settingEnum: SettingsKeyEnum): PrivacySettingModel? {
    return this.find { it.key == settingEnum.key }
}
