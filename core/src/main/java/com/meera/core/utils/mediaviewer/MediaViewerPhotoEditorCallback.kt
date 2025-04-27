package com.meera.core.utils.mediaviewer

import android.net.Uri
import com.meera.media_controller_common.MediaControllerOpenPlace

interface MediaViewerPhotoEditorCallback {

    fun onOpenPhotoEditor(
        imageUrl: Uri,
        type: MediaControllerOpenPlace,
        supportGifEditing: Boolean,
        resultCallback: MediaViewerPhotoEditorResultCallback
    )

    fun onAddHashSetVideoToDelete(path: String)

    interface MediaViewerPhotoEditorResultCallback {
        fun onPhotoReady(resultUri: Uri)
        fun onVideoReady(resultUri: Uri)
        fun onError()
        fun onCanceled() {}
    }
}
