package com.meera.core.utils.tedbottompicker.models

import android.net.Uri

data class MediaViewerEditedAttachmentInfo(
    val original: Uri? = null,
    val edited: Uri? = null
) {
    companion object {
        fun empty() = MediaViewerEditedAttachmentInfo()
    }
}
