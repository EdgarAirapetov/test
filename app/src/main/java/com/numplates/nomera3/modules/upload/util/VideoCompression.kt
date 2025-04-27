package com.numplates.nomera3.modules.upload.util

import android.content.Context
import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.meera.media_controller_common.CropInfo
import com.numplates.nomera3.modules.upload.VideoConverter
import com.numplates.nomera3.modules.upload.VideoConverterListener
import timber.log.Timber
import java.io.File
import kotlin.coroutines.suspendCoroutine

suspend fun compressVideo(
    context: Context,
    videoConverter: VideoConverter,
    cacheDir: File,
    source: Uri,
    fileManager: FileManager,
    cropInfo: CropInfo?
): String {
    return suspendCoroutine { cont ->
        val compressed: File? = fileManager.duplicateTo(source, cacheDir)
        if (compressed != null) {
            val listener = object : VideoConverterListener {
                override fun onStarted() = Unit
                override fun onProgress(progress: Float) = Unit
                override fun onCompleted() {
                    cont.resumeWith(Result.success(compressed.path))
                }

                override fun onCancelled() = Unit
                override fun onError(cause: Throwable?) {
                    Timber.e(cause ?: Exception("Error during video conversion."))
                    cont.resumeWith(Result.success(source.path.orEmpty()))
                }
            }

            videoConverter.compressVideo(
                context = context,
                srcUri = source,
                destination = compressed.path,
                cropInfo = cropInfo,
                baseListener = listener
            )
        } else {
            cont.resumeWith(Result.failure(Exception("File does not exist")))
        }
    }
}
