package com.numplates.nomera3.modules.bump.ui.entity

data class UserShakeUiState(
    val shakeUser: UserShakeUiModel,
    val dotsCount: Int,
    val isNeedToShowDotsIndicator: Boolean,
    val selectedPosition: Int
)
