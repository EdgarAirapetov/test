package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class BlockSuggestionDto(
    @SerializedName("blocked_id")
    val blockedId: Long
)
