package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RandomAvatarResponse(
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("animation")
    val animation: String
): Parcelable
