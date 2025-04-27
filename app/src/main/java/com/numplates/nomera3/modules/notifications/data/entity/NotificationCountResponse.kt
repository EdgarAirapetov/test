package com.numplates.nomera3.modules.notifications.data.entity

import com.google.gson.annotations.SerializedName

data class NotificationCountResponse(
        @SerializedName("count")
        val count: Int?
)
