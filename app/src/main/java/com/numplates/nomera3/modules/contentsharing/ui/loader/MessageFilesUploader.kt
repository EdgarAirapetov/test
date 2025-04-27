package com.numplates.nomera3.modules.contentsharing.ui.loader

import android.content.Context
import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.MediaType
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatImageUseCase
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatVideoUseCase
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject


class MessageFilesUploader @Inject constructor(
    private val partialUploadChatImageUseCase: PartialUploadChatImageUseCase,
    private val partialUploadChatVideoUseCase: PartialUploadChatVideoUseCase,
    private val appContext: Context,
    private val fileManager: FileManager,
) {

    suspend fun uploadMediaFiles(
        uris: List<Uri>,
        progress: suspend (Float) -> Unit
    ): List<ChatAttachmentPartialUploadModel> {
        val attachments = mutableListOf<ChatAttachmentPartialUploadModel>()
        val totalFilesLength = uris.sumOf { uri -> fileManager.getFileSize(uri) }
        var currentFilesProgress = 0f
        uris.forEach { uri ->
            yield()
            val file = File(requireNotNull(fileManager.getRealPathFromUri(uri)))
            val type = requireNotNull(appContext.contentResolver.getType(uri))

            val result = if (type.startsWith(MediaType.IMAGE.value)) {
                partialUploadChatImageUseCase.invoke(file) { value ->
                    progress(((currentFilesProgress + value * file.length()) / totalFilesLength))
                }
            } else if (type.startsWith(MediaType.VIDEO.value)) {
                partialUploadChatVideoUseCase.invoke(file) { value ->
                    progress(((currentFilesProgress + value * file.length()) / totalFilesLength))
                }
            } else {
                error("Unsupported media type: $type")
            }
            attachments.add(result)
            currentFilesProgress += file.length()
            progress((currentFilesProgress / totalFilesLength))
        }
        return attachments
    }
}
