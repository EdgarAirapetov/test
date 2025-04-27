package com.meera.media_controller_implementation.presentation

import android.net.Uri
import com.noomeera.nmrmediasdk.NMRMediaSDK
import com.noomeera.nmrmediatools.utils.CropMode
import com.noomeera.nmrmediatools.utils.NMRMediaSettings
import com.noomeera.nmrmediatools.utils.Ratio

internal sealed class MediaControllerWrapperEvent {
    data class OpenExternalEditor(
        val uri: Uri?,
        val ratios: List<Ratio>,
        val mediaType: NMRMediaSDK.MediaType,
        val maxDuration: Long,
        val cropMode: CropMode,
        val forceResize: Boolean,
        val mediaSettings: NMRMediaSettings
    ) : MediaControllerWrapperEvent()

    data class OpenMomentsExternalEditor(
        val mediaSettings: NMRMediaSettings
    ): MediaControllerWrapperEvent()
}
