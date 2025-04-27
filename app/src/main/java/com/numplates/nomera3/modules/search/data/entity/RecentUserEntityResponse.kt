package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple

data class RecentUserEntityResponse(

    @SerializedName("type")
    val type: String?,

    @SerializedName("data")
    val data: UserSimple?,

    @SerializedName("happened_at")
    val happenedAt: Long?
)
