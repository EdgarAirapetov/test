package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class GreetingResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("room_id")
    val roomId: Long
)

fun GreetingResponse.toGreetingModel(): GreetingModel = GreetingModel(id, roomId)