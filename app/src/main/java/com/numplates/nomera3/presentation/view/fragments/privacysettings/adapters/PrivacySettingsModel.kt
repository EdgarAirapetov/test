package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel

data class PrivacySettingsModel(
    val viewType: Int,
    val isEnabled: Boolean,
    val settings: List<PrivacySettingUiModel>? = null
)
