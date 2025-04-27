package com.numplates.nomera3.modules.upload.domain.usecase.post

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UpdateGalleryUseCase @Inject constructor(val context: Context) {
    fun execute(path: String?) {
        path?.let {
            try {
                val file = File(it)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.toString()), null
                )
                { path, _ ->
                    Timber.d("ExternalStorage Scanned $path")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } ?: kotlin.run {
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(Environment.getExternalStorageDirectory().toString()), null
                )
                { path, _ ->
                    Timber.d("ExternalStorage Scanned $path")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
