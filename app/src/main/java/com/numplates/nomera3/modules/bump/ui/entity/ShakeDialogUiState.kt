package com.numplates.nomera3.modules.bump.ui.entity

import androidx.annotation.StringRes

data class ShakeDialogUiState(
    @StringRes val shakeLabelTextRes: Int,
    @StringRes val shakeMessageTextRes: Int,
    val isShowTurnOnAccurateLocationButton: Boolean = false,
    val isShowShakeLocationEnableDescription: Boolean = false,
    val shakeDialogStatus: ShakeDialogStatus = ShakeDialogStatus.NOT_FOUND
)
