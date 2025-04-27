package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class ResponseChatMediaUpload(
    val link: String?,
    val type: String?,
    val data: MediaData?
)

data class MediaData(
    @SerializedName("ratio")
    val ratio: Double,
    @SerializedName("size")
    val size: String,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("is_silent")
    val isSilent: Boolean? = null,
    @SerializedName("low_quality")
    val lowQuality: String? = null,
    @SerializedName("preview")
    val preview: String? = null
)
