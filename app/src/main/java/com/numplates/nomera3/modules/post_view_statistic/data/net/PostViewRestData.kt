package com.numplates.nomera3.modules.post_view_statistic.data.net

import com.google.gson.annotations.SerializedName

data class PostViewRestData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("group_id")
    val groupId: Int,
    @SerializedName("road_type")
    val roadType: String,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("view_at")
    val viewAt: Long
)