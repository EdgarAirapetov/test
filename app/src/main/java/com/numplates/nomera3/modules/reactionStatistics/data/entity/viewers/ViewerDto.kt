package com.numplates.nomera3.modules.reactionStatistics.data.entity.viewers

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.reactionStatistics.data.entity.UserDto

data class ViewerDto(
    @SerializedName("reaction") val reaction: String,
    @SerializedName("user") val user: UserDto,
    @SerializedName("viewed_at") val viewedAt: Long
)
