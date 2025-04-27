package com.numplates.nomera3.modules.notifications.data.entity

import com.google.gson.annotations.SerializedName

data class AvatarMetaEntityResponse(
        @SerializedName("big")
        val big: String?,

        @SerializedName("small")
        val small: String?
)
