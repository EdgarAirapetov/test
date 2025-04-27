package com.numplates.nomera3.presentation.model.enums

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySettingVisibility

enum class SettingsUserTypeEnum(val key: Int) {
    NOBODY(0),
    ALL(1),
    FRIENDS(2);

    companion object {
        fun fromKey(key: Int): SettingsUserTypeEnum = values().first { it.key == key }
    }
}

fun SettingsUserTypeEnum.toAmplitudePropertySettingVisibility(): AmplitudePropertySettingVisibility {
    return when(this) {
        SettingsUserTypeEnum.NOBODY -> AmplitudePropertySettingVisibility.NOBODY
        SettingsUserTypeEnum.ALL -> AmplitudePropertySettingVisibility.ALL
        SettingsUserTypeEnum.FRIENDS -> AmplitudePropertySettingVisibility.FRIENDS
    }
}
