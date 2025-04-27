package com.numplates.nomera3.modules.upload

import android.content.Context
import android.net.Uri
import com.meera.media_controller_common.CropInfo

interface VideoConverter {
    fun compressVideo(
        context: Context,
        srcUri: Uri,
        destination: String,
        cropInfo: CropInfo?,
        baseListener: VideoConverterListener
    )

    fun getMaxBitrate(duration: Long): Int
}
