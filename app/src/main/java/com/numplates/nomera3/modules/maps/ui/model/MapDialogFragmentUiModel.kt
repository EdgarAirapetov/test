package com.numplates.nomera3.modules.maps.ui.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MapDialogFragmentUiModel(
    @StringRes
    val titleResId: Int,
    @StringRes
    val messageResId: Int,
    @StringRes
    val actionResId: Int,
    @DrawableRes
    val imageResId: Int
)
