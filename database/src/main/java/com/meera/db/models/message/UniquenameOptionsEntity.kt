package com.meera.db.models.message

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniquenameOptionsEntity(

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("group_id")
    val groupId: Long?,                     // !!! Reserved to future

    @SerializedName("symbol")
    val symbol: String?,

    @SerializedName("url")
    val url: String?

) : Parcelable
