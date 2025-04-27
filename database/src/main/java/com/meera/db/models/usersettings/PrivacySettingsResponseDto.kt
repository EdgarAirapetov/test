package com.meera.db.models.usersettings

import com.google.gson.annotations.SerializedName

data class PrivacySettingsResponseDto(
    @SerializedName("settings")
    val settings: List<PrivacySettingDto>
)
