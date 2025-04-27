package com.numplates.nomera3.modules.bump.ui.entity

sealed class ShakeBottomDialogUiEffect {

    object AnimateFadeUiEffect : ShakeBottomDialogUiEffect()

    object AnimateShakeUiEffect : ShakeBottomDialogUiEffect()

    object ResetShakeViewsPosition : ShakeBottomDialogUiEffect()
}
