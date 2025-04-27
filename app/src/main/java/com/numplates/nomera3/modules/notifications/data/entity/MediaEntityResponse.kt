package com.numplates.nomera3.modules.notifications.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaEntityResponse(
    @SerializedName("artist")
    var artist: String? = null,

    @SerializedName("track")
    var track: String? = null
) : Parcelable