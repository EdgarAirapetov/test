package com.numplates.nomera3.modules.purchase.ui.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GiftCategoryUiModel(
    val categoryName: String,
    val gifts: List<GiftItemUiModel>,
) : Parcelable
