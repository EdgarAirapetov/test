package com.meera.media_controller_api

import android.net.Uri
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace

interface MediaControllerFeatureApi {
    fun open(
        uri: Uri? = null,
        openPlace: MediaControllerOpenPlace,
        callback: MediaControllerCallback,
        openStickers: Boolean = false
    )

    fun showVideoTooLongDialog(
        openPlace: MediaControllerOpenPlace,
        needEditResponse: MediaControllerNeedEditResponse.VideoTooLong,
        showInMinutes: Boolean,
        openEditorCallback: () -> Unit
    )

    fun needEditMedia(
        uri: Uri?,
        openPlace: MediaControllerOpenPlace
    ): MediaControllerNeedEditResponse
}
