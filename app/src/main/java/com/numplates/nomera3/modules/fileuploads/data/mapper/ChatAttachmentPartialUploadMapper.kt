package com.numplates.nomera3.modules.fileuploads.data.mapper

import com.numplates.nomera3.modules.fileuploads.data.model.ChatAttachmentPartialUploadDto
import com.numplates.nomera3.modules.fileuploads.data.model.ChatMetadataPartialUploadDto
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatMetadataPartialUploadModel
import javax.inject.Inject

class ChatAttachmentPartialUploadMapper @Inject constructor() {

    fun mapChatAttachmentPartialUpload(data: ChatAttachmentPartialUploadDto): ChatAttachmentPartialUploadModel {
        return ChatAttachmentPartialUploadModel(
            metadata = data.metadata?.let(::mapMetadata),
            preview = data.preview,
            sourceType = data.sourceType,
            type = data.type,
            url = data.url
        )
    }

    private fun mapMetadata(data: ChatMetadataPartialUploadDto): ChatMetadataPartialUploadModel {
        return ChatMetadataPartialUploadModel(
            duration = data.duration,
            isSilent = data.isSilent,
            lowQuality = data.lowQuality,
            preview = data.preview,
            ratio = data.aspect
        )
    }
}
