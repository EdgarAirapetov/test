package com.numplates.nomera3.modules.fileuploads.domain

import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import okhttp3.MediaType
import java.io.File

interface FileUploadRepository {

    suspend fun partialUploadChatVideo(
        fileToUpload: File,
        progress: suspend (Float) -> Unit
    ): ChatAttachmentPartialUploadModel

    suspend fun partialUploadChatImage(
        fileToUpload: File,
        progress: suspend (Float) -> Unit
    ): ChatAttachmentPartialUploadModel

    suspend fun partialFileUpload(
        fileToUpload: File,
        mediaType: MediaType?,
        sourceType: PartialUploadSourceType,
        progress: suspend (Float) -> Unit = {}
    ): String

    suspend fun getChatAttachmentPartialUpload(uploadId: String): ChatAttachmentPartialUploadModel
}
