package com.numplates.nomera3.modules.upload.domain.usecase.post

import android.content.Context
import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.meera.media_controller_common.CropInfo
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.modules.upload.VideoConverter
import com.numplates.nomera3.modules.upload.util.compressVideo
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class CompressVideoUseCase @Inject constructor(
    private val appContext: Context,
    @Named(CACHE_DIR) private val cacheDir: File,
    private val videoConverter: VideoConverter,
    private val fileManager: FileManager
) {

    suspend fun execute(
        videoSource: String,
        cropInfo: CropInfo? = null
    ): String {
        return compressVideo(
            context = appContext,
            videoConverter = videoConverter,
            cacheDir = cacheDir,
            source = Uri.parse(videoSource),
            cropInfo = cropInfo,
            fileManager = fileManager
        )
    }
}
