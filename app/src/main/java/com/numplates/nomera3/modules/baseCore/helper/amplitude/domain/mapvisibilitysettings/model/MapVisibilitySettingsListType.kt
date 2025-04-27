package com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMapPrivacyListType

enum class MapVisibilitySettingsListType {
    BLACKLIST, WHITELIST
}

fun MapVisibilitySettingsListType.toAmplitudePropertyMapPrivacyListType(): AmplitudePropertyMapPrivacyListType {
    return when (this) {
        MapVisibilitySettingsListType.BLACKLIST -> AmplitudePropertyMapPrivacyListType.NEVER
        MapVisibilitySettingsListType.WHITELIST -> AmplitudePropertyMapPrivacyListType.ALWAYS
    }
}