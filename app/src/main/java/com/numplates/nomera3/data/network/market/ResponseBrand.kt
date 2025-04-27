package com.numplates.nomera3.data.network.market

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ResponseBrand(
        val brands: List<Brand>
):Parcelable

@Parcelize
data class Brand(
        val avatar: String,
        val id: Int,
        val name: String
):Parcelable