package com.numplates.nomera3.data.newmessenger.response


import com.google.gson.annotations.SerializedName
import com.meera.db.models.chatmembers.UserEntity


data class ResponseGroupChatTyping(

    @SerializedName("room_id")
    var roomId: Long,

    @SerializedName("type")
    var type: String,

    @SerializedName("user")
    var user: UserEntity
)
