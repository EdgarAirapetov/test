package com.numplates.nomera3.modules.usersettings.domain.models

data class PrivacySettingModel(
    val key: String,
    val value: Int?,
    val countBlacklist: Int? = null,
    val countWhitelist: Int? = null,
)
