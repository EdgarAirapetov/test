package com.numplates.nomera3.modules.fileuploads.domain.usecase

import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import javax.inject.Inject


class GetChatAttachmentPartialUploadUseCase @Inject constructor(
    private val repository: FileUploadRepository
) {

    suspend fun invoke(uploadId: String): ChatAttachmentPartialUploadModel {
        return repository.getChatAttachmentPartialUpload(uploadId)
    }

}
