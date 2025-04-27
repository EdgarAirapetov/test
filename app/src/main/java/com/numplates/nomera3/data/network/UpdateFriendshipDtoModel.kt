package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateFriendshipDtoModel(
    @SerializedName("friend_id")
    val friendId: Long,
    @SerializedName("action")
    val action: String
) : Parcelable
