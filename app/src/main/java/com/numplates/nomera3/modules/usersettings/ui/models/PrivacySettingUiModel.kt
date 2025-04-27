package com.numplates.nomera3.modules.usersettings.ui.models

data class PrivacySettingUiModel(
    val key: String,
    val value: Int?,
    val countBlacklist: Int? = null,
    val countWhitelist: Int? = null,
)
