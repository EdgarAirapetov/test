package com.numplates.nomera3.modules.tags.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple

data class UniqueNameTagListResponse(
        @SerializedName("users") val users: List<UserSimple>?
)
