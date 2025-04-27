package com.numplates.nomera3.modules.complains.data.model

import com.google.gson.annotations.SerializedName

data class AttachMediaDto(
    @SerializedName("file") val file: MediaFileDto
)

data class MediaFileDto(
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
)
