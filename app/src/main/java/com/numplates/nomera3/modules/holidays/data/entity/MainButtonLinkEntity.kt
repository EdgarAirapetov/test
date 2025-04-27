package com.numplates.nomera3.modules.holidays.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainButtonLinkEntity(
    @SerializedName("default")
    val default: String?,
    @SerializedName("active")
    val active: String?
): Parcelable