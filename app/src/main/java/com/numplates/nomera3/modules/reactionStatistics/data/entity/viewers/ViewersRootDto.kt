package com.numplates.nomera3.modules.reactionStatistics.data.entity.viewers

import com.google.gson.annotations.SerializedName

data class ViewersRootDto(
    @SerializedName("count") val count: Long,
    @SerializedName("more") val more: Int,
    @SerializedName("viewers") val viewers: List<ViewerDto>
)
