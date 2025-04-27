package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName

data class RecentHashtagEntityResponse(

        @SerializedName("type")
        val type: String?,

        @SerializedName("data")
        val data: RecentHashtag?,

        @SerializedName("happened_at")
        val happenedAt: Long?
)

data class RecentHashtag(

        @SerializedName("id")
        val id: Int?,

        @SerializedName("text")
        val text: String?,

        @SerializedName("count")
        val count: Int?
)
