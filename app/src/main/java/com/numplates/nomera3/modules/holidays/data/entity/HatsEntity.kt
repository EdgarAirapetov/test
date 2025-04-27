package com.numplates.nomera3.modules.holidays.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HatsEntity(
    @SerializedName("general")
    val general: String?,
    @SerializedName("premium")
    val premium: String?,
    @SerializedName("vip")
    val vip: String?
): Parcelable