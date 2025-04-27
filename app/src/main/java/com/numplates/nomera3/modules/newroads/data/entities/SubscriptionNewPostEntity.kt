package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.annotations.SerializedName

data class SubscriptionNewPostEntity(
    @SerializedName("has_new")
    val hasNew: Boolean
)