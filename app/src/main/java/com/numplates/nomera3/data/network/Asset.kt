package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Asset(

    @SerializedName("url")
    var url: String?,

    @SerializedName("type")
    var type: String?,

    @SerializedName("source_type")
    var sourceType: String?,

    @SerializedName("metadata")
    var metadata: AssetMetadata

) : Parcelable


@Parcelize
data class AssetMetadata(
    @SerializedName("aspect")
    val aspect: Float?,

    @SerializedName("duration")
    val duration: Int?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("small_url")
    val smallUrl: String?
) : Parcelable
