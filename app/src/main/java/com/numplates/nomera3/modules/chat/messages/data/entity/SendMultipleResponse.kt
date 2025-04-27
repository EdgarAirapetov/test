package com.numplates.nomera3.modules.chat.messages.data.entity

import com.google.gson.annotations.SerializedName

data class SendMultipleResponse(
    @SerializedName("user_names")
    val userNames: List<String>?
)
