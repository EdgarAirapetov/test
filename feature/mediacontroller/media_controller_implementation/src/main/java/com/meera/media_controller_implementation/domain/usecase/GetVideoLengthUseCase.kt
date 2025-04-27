package com.meera.media_controller_implementation.domain.usecase

import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import java.io.File
import javax.inject.Inject

internal class GetVideoLengthUseCase @Inject constructor(
    private val fileManager: FileManager
) {
    fun execute(videoUri: Uri): Long {
        return if (fileManager.getMediaType(videoUri) == MEDIA_TYPE_VIDEO) {
            (fileManager.getVideoDurationMils(videoUri) / 1000)
        } else 0
    }

    fun execute(videoSource: String): Long {
        val videoUri = getUri(videoSource)

        return execute(videoUri)
    }

    private fun getUri(path: String): Uri {
        return try {
            Uri.fromFile(File(path))
        } catch (ex: Exception) {
            Uri.parse(path)
        }
    }
}
