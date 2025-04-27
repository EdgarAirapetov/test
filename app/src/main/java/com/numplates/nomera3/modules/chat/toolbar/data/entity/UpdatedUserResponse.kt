package com.numplates.nomera3.modules.chat.toolbar.data.entity

import com.google.gson.annotations.SerializedName

data class UpdatedUserResponse(

        @SerializedName("room_id")
        val roomId: Long?,

        @SerializedName("user_id")
        val userId: Long?
)
