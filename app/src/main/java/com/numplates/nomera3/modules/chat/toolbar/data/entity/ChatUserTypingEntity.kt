package com.numplates.nomera3.modules.chat.toolbar.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.chatmembers.UserEntity

data class ChatUserTypingEntity(

    @SerializedName("room_id")
    var roomId: Long,

    @SerializedName("type")
    var type: String,

    @SerializedName("user")
    var user: UserEntity                // TODO: 22.12.2021 Переделать на укороченный ответ User
)
