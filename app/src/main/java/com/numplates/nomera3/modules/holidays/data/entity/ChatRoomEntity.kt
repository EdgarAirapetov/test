package com.numplates.nomera3.modules.holidays.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoomEntity(
    @SerializedName("type")
    val type: String?,
    @SerializedName("background_dialog")
    val background_dialog: String?,
    @SerializedName("background_anon")
    val background_anon: String?,
    @SerializedName("background_group")
    val background_group: String?,
): Parcelable