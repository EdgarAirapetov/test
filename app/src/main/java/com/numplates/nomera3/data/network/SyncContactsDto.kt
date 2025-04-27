package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class SyncContactsDto(
    @SerializedName("phones")
    val phones: List<String>
)


data class SyncContactsResultDto(
    @SerializedName("found") val found: Int,
    @SerializedName("processed") val processed: Int,
)
