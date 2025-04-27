package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple

data class ProfileSuggestionsDto(
    @SerializedName("users")
    val users: List<UserSimple>
)
