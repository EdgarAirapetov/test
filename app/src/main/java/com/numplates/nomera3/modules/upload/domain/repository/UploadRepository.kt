package com.numplates.nomera3.modules.upload.domain.repository

import com.meera.db.models.UploadBundle
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.upload.domain.UploadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface UploadRepository {
    fun upload(type: UploadType, uploadBundle: UploadBundle): Job
    fun retryLastFailed()
    fun getState(): Flow<UploadState>
    fun isNowUploading():Boolean
}
