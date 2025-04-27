package com.meera.db.models.message

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniquenameEntity(

    @SerializedName("id")
    val id: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("opts")
    val options: UniquenameOptionsEntity?,

    @SerializedName("text")
    val text: String?

) : Parcelable
