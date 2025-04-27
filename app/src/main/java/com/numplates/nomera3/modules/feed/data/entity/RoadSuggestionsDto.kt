package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto

data class RoadSuggestionsDto(
    @SerializedName("users")
    val users: List<UserSimpleDto>
)
