package com.numplates.nomera3.data.network.market

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class  ResponseWizard(
       @SerializedName("fields") val fields: List<Field>
): Parcelable

@Parcelize
data class Field(
        @SerializedName("id_field") val id_field: Int,
        @SerializedName("name") val name: String,
        @SerializedName("type") val type: String,
        @SerializedName("units") val units: String,
        @SerializedName("value") val value: List<Value>
):Parcelable{
    companion object{
        const val TYPE_TEXT = "text"
        const val TYPE_COLOR = "color"
        const val TYPE_LIST = "list"
    }
}

@Parcelize
data class Value(
        @SerializedName("id_color") val id_color: Int,
        @SerializedName("name") val name: String,
        @SerializedName("value") val value: String
):Parcelable