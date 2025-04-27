package com.numplates.nomera3.modules.moments.show.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentLinkResponseDto(
    @SerializedName("deep_link_url")
    val deepLinkUrl: String
): Parcelable
