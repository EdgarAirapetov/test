package com.numplates.nomera3.modules.gift_coffee.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PromoCodeEntity(
        @SerializedName("code")
        val code: String
) : Parcelable