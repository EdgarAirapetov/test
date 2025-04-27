package com.numplates.nomera3.modules.purchase.ui.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GiftItemUiModel(
    val giftId: Long,
    val marketProductId: String,
    val smallImage: String,
    val image: String?,
    val customTitle: String,
    val type: Int,
    val customDesc: String? = null,
    val price: String?,
) : Parcelable
