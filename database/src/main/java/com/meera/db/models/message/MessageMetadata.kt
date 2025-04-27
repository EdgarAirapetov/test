package com.meera.db.models.message

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class MessageMetadata(

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("at")
    val createdAt: Long? = null,

    // Optional field -------------------

    @SerializedName("caller")
    val caller: MetadataCaller? = null,

    @SerializedName("duration")
    val callDuration: Int? = null,

    @SerializedName("user_id")
    val userId: Long? = null


) : Parcelable {
    constructor() : this(null, null, null)
}

@Parcelize
data class MetadataCaller(
    @SerializedName("id")
    val callerId: Long
) : Parcelable
