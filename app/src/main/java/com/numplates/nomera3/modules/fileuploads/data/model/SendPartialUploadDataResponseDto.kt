package com.numplates.nomera3.modules.fileuploads.data.model

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.PartialUploadCompletionState

data class SendPartialUploadDataResponseDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("completed")
    val completed: Int?,
    @SerializedName("bytes")
    val bytes: Long?,
    @SerializedName("bytes_total")
    val bytesTotal: Long?
) {
    fun isCompleted() = completed == PartialUploadCompletionState.COMPLETE.value
}
