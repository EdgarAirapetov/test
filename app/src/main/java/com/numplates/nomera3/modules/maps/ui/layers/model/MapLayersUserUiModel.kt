package com.numplates.nomera3.modules.maps.ui.layers.model

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class MapLayersUserUiModel(
    val accountType: AccountTypeEnum,
    val accountColor: Int,
    val avatarUrl: String?
)
