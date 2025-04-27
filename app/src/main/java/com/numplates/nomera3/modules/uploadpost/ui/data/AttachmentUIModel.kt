package com.numplates.nomera3.modules.uploadpost.ui.data

import android.net.Uri
import android.os.Parcelable
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.numplates.nomera3.modules.feed.ui.entity.MediaPositioning
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import kotlinx.parcelize.Parcelize

@Parcelize
data class UIAttachmentPostModel(
    val type: AttachmentPostType,
    val attachmentResource: String,
    val attachmentWidth: Int,
    val attachmentHeight: Int
) : Parcelable

@Parcelize
data class UIAttachmentMediaModel(
    val type: AttachmentPostType,
    val initialUri: Uri,
    val editedUri: Uri? = null,
    val attachmentWidth: Int,
    val attachmentHeight: Int,
    val mediaPositioning: MediaPositioning = MediaPositioning(0.0, 0.0),
    val isCompressed: Boolean = false,
    val networkId: String? = null
) : Parcelable {
    fun getActualResource() = (editedUri?: initialUri).path.orEmpty()
    fun isEdited() = editedUri != null
    fun getActualUri(): Uri {
        return editedUri ?: initialUri
    }

    fun isAttachmentCompressed() = isCompressed || isEdited()
}

fun UIAttachmentMediaModel.isSameMedia(mediaUriModel: MediaUriModel): Boolean {
    return (mediaUriModel.initialUri == this.initialUri
        && mediaUriModel.editedUri == this.editedUri)
        || (mediaUriModel.networkId != null && this.networkId == mediaUriModel.networkId)
}

fun UIAttachmentMediaModel.toMediaUriModel(): MediaUriModel {
    return MediaUriModel(
        initialUri = this.initialUri,
        editedUri = this.editedUri,
        networkId = this.networkId
    )
}

fun UIAttachmentMediaModel.parseTypeForAdapter(): Int {
    return when (this.type) {
        AttachmentPostType.ATTACHMENT_VIDEO -> RecyclingPagerAdapter.VIEW_TYPE_VIDEO
        else -> RecyclingPagerAdapter.VIEW_TYPE_IMAGE
    }
}

enum class AttachmentPostType {
    ATTACHMENT_PHOTO,
    ATTACHMENT_GIF,
    ATTACHMENT_PREVIEW,
    ATTACHMENT_VIDEO
}



