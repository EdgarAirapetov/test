package com.meera.media_controller_api.model

import android.net.Uri
import com.meera.media_controller_common.MediaEditorResult
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude

interface MediaControllerCallback {
    fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {}
    fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {}
    fun onMediaListReady(results: List<MediaEditorResult>) {}
    fun onCanceled() {}
    fun onError() {}
}
