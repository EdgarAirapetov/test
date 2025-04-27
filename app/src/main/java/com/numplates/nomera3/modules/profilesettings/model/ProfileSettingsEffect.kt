package com.numplates.nomera3.modules.profilesettings.model

sealed class ProfileSettingsEffect {

    data class SupportUserIdFound(val userId: Long) : ProfileSettingsEffect()
    data class AboutMeeraUserIdFound(val userId: Long) : ProfileSettingsEffect()
}
