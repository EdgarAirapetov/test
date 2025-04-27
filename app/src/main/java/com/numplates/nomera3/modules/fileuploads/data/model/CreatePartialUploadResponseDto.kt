package com.numplates.nomera3.modules.fileuploads.data.model

import com.google.gson.annotations.SerializedName

data class CreatePartialUploadResponseDto(
    @SerializedName("id")
    val partialUploadId: String
)
