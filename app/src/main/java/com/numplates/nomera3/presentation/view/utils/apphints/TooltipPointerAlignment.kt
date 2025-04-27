package com.numplates.nomera3.presentation.view.utils.apphints

sealed class TooltipPointerAlignment {
    object Top : TooltipPointerAlignment()
    object Bottom : TooltipPointerAlignment()
    object None : TooltipPointerAlignment()
}