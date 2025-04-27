package com.numplates.nomera3.modules.chat.data

import com.google.gson.annotations.SerializedName

data class ChatUpdatedUserEntity(

    @SerializedName("room_id")
    val roomId: Long? = null,

    @SerializedName("user_id")
    val userId: Long? = null
)