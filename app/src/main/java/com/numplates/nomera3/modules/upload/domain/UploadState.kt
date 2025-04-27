package com.numplates.nomera3.modules.upload.domain

import com.meera.db.models.UploadItem

data class UploadState(val status: UploadStatus, val uploadItem: UploadItem)

sealed interface UploadStatus {
    object Processing : UploadStatus
    data class Failed(val maxTriesReached: Boolean) : UploadStatus
    object Success : UploadStatus
}
