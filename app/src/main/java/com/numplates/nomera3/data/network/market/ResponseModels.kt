package com.numplates.nomera3.data.network.market

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResponseModels(
        val models: List<Model>
):Parcelable

@Parcelize
data class Model(
        val id: Int,
        val name: String
):Parcelable