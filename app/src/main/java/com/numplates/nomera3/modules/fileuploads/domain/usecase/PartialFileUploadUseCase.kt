package com.numplates.nomera3.modules.fileuploads.domain.usecase

import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import okhttp3.MediaType
import java.io.File
import javax.inject.Inject

class PartialFileUploadUseCase @Inject constructor(
    private val repository: FileUploadRepository
) {
    suspend fun invoke(
        fileToUpload: File,
        mediaType: MediaType?,
        sourceType: PartialUploadSourceType,
        progress: (Float) -> Unit = {}
    ): String = repository.partialFileUpload(
        fileToUpload,
        mediaType,
        sourceType,
        progress
    )
}
