package com.numplates.nomera3.modules.purchase.ui.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SimplePurchaseUiModel(
    val marketId: String,
    val description: String,
    val price: String,
) : Parcelable
