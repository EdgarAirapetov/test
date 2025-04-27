package com.numplates.nomera3.presentation.model.webresponse

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResponseVehicleTypes (
        @SerializedName("types") val types: List<Type>
): Parcelable

@Parcelize
data class Type(
        @SerializedName("id") val typeId: Int,
        @SerializedName("name") val name: String,
        @SerializedName("avatar") val imageUrl: String
): Parcelable