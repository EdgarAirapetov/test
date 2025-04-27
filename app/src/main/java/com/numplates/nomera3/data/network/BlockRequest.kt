package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class BlockRequest(
        @SerializedName("user_id") val userId: Long,
        @SerializedName("block") val block: Boolean
)