package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum

data class SettingsPrivateMessagesState(
    val whiteListCount: Int,
    val blackListCount: Int,
    val settingsType: SettingsUserTypeEnum
)