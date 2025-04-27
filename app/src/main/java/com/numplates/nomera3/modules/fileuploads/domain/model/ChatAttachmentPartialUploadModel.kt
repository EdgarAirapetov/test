package com.numplates.nomera3.modules.fileuploads.domain.model

import com.google.gson.annotations.SerializedName

data class ChatAttachmentPartialUploadModel(

    @SerializedName("metadata")
    val metadata: ChatMetadataPartialUploadModel?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("source_type")
    val sourceType: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("url")
    val url: String?
)

data class ChatMetadataPartialUploadModel(

    @SerializedName("duration")
    val duration: Int?,

    @SerializedName("is_silent")
    val isSilent: Boolean?,

    @SerializedName("low_quality")
    val lowQuality: String?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("ratio")
    val ratio: Double?
)
