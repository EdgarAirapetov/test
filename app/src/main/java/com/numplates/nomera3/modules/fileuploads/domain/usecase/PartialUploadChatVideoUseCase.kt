package com.numplates.nomera3.modules.fileuploads.domain.usecase

import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import java.io.File
import javax.inject.Inject

class PartialUploadChatVideoUseCase @Inject constructor(
    private val repository: FileUploadRepository
) {

    suspend fun invoke(fileToUpload: File, progress: suspend (Float) -> Unit = {}): ChatAttachmentPartialUploadModel {
        return repository.partialUploadChatVideo(fileToUpload, progress)
    }

}
