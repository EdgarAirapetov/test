package com.numplates.nomera3.modules.peoples.ui.dialog.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllowContactsSyncUiState(
    @StringRes val labelRes: Int? = null,
    @StringRes val messageRes: Int? = null,
    @StringRes val positiveButtonText: Int? = null,
    @StringRes val negativeButtonText: Int? = null,
    @DrawableRes val iconRes: Int? = null
) : Parcelable
