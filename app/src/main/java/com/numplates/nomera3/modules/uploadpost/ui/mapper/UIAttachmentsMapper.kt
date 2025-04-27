package com.numplates.nomera3.modules.uploadpost.ui.mapper

import android.net.Uri
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UIAttachmentsMapper @Inject constructor(
    private val metaDataDelegate: MediaFileMetaDataDelegate
) {

    suspend fun mapImageToAttachment(path: String, isPreviewImage: Boolean = false): UIAttachmentPostModel? =
        withContext(Dispatchers.Default) {
        val imageMetadata = metaDataDelegate.getImageMetadata(Uri.parse(path).path.orEmpty())
        val width = imageMetadata?.width
        val height = imageMetadata?.height
        if (height == null || width == null || width == 0 || height == 0) return@withContext null
        UIAttachmentPostModel(
            type = getImageType(path, isPreviewImage),
            attachmentResource = path,
            attachmentHeight = height,
            attachmentWidth = width
        )
    }

    suspend fun mapImageMediaToAttachment(media: MediaUriModel, isPreviewImage: Boolean = false): UIAttachmentMediaModel? =
        withContext(Dispatchers.Default) {
            val imageMetadata = metaDataDelegate.getImageMetadata(media.getActualUri().path.orEmpty())
            val width = imageMetadata?.width
            val height = imageMetadata?.height
            if (height == null || width == null || width == 0 || height == 0) return@withContext null
            UIAttachmentMediaModel(
                type = getImageType(media.getActualUri().path.orEmpty(), isPreviewImage),
                initialUri = media.initialUri,
                editedUri = media.editedUri,
                attachmentHeight = height,
                attachmentWidth = width
            )
        }

    suspend fun mapVideoToAttachment(path: String): UIAttachmentPostModel? = withContext(Dispatchers.Default) {
        val videoMetadata = metaDataDelegate.getVideoMetadata(Uri.parse(path))
        val width = videoMetadata?.width
        val height = videoMetadata?.height
        if (width == null || height == null) return@withContext null
        UIAttachmentPostModel(
            type = AttachmentPostType.ATTACHMENT_VIDEO,
            attachmentResource = path,
            attachmentHeight = height,
            attachmentWidth = width
        )
    }

    suspend fun mapVideoMediaToAttachment(media: MediaUriModel): UIAttachmentMediaModel? = withContext(Dispatchers.Default) {
        val videoMetadata = metaDataDelegate.getVideoMetadata(media.getActualUri())
        val width = videoMetadata?.width
        val height = videoMetadata?.height
        if (width == null || height == null) return@withContext null
        UIAttachmentMediaModel(
            type = AttachmentPostType.ATTACHMENT_VIDEO,
            initialUri = media.initialUri,
            editedUri = media.editedUri,
            attachmentHeight = height,
            attachmentWidth = width
        )
    }

    private fun getImageType(path: String, isPreviewImage: Boolean) =
        if (isPreviewImage) {
            AttachmentPostType.ATTACHMENT_PREVIEW
        } else if (metaDataDelegate.isGifImage(path)) {
            AttachmentPostType.ATTACHMENT_GIF
        } else {
            AttachmentPostType.ATTACHMENT_PHOTO
        }
}
