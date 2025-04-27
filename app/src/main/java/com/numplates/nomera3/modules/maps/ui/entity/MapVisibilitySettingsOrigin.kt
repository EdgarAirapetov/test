package com.numplates.nomera3.modules.maps.ui.entity

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMapPrivacy


enum class MapVisibilitySettingsOrigin {
    USER_PROFILE,
    SETTINGS,
    MAP;

    companion object {
        const val ARG = "ARG_MAP_VISIBILITY_SETTINGS_ORIGIN"
    }
}

fun MapVisibilitySettingsOrigin.toAmplitudePropertyWhereMapPrivacy(): AmplitudePropertyWhereMapPrivacy {
    return when (this) {
        MapVisibilitySettingsOrigin.USER_PROFILE -> AmplitudePropertyWhereMapPrivacy.PROFILE
        MapVisibilitySettingsOrigin.SETTINGS -> AmplitudePropertyWhereMapPrivacy.SETTINGS
        MapVisibilitySettingsOrigin.MAP -> AmplitudePropertyWhereMapPrivacy.MAP
    }
}