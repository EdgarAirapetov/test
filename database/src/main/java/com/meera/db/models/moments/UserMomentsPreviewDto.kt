package com.meera.db.models.moments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMomentsPreviewDto(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("viewed") val viewed: Int
) : Parcelable
