package com.numplates.nomera3.data.fcm.models


import com.google.gson.annotations.SerializedName
import com.meera.core.extensions.empty

data class PushCallObject(
    @SerializedName("is_video")
    val isVideo: Boolean?,
    @SerializedName("message_id")
    val messageId: String?,
    @SerializedName("room_id")
    val roomId: Int?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("name")
    val name: String? = String.empty(),
    @SerializedName("avatar_big")
    val avatarBig: String? = String.empty(),
    @SerializedName("start_call")
    val startCall: Boolean?
)
