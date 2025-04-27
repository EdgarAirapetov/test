package com.numplates.nomera3.modules.fileuploads.data.model

import com.google.gson.annotations.SerializedName

data class ChatAttachmentPartialUploadDto(

    @SerializedName("metadata")
    val metadata: ChatMetadataPartialUploadDto?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("source_type")
    val sourceType: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("url")
    val url: String?

)

data class ChatMetadataPartialUploadDto(

    @SerializedName("duration")
    val duration: Int?,

    @SerializedName("is_silent")
    val isSilent: Boolean?,

    @SerializedName("low_quality")
    val lowQuality: String?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("aspect")
    val aspect: Double?
)
