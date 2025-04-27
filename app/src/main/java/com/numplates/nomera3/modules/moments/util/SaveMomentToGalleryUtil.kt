package com.numplates.nomera3.modules.moments.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.R
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume

private const val MOMENTS_FOLDER = "/moments/"

class SaveMomentToGalleryUtil @Inject constructor(
    private val context: Context,
    private val fileManager: FileManager
) {

    private val momentsDirectoryName: String
        get() = context.getString(R.string.app_name) + MOMENTS_FOLDER

    /**
     * Продублировать медиа момента в папку с моментами
     */
    fun duplicateMomentAndAddToGallery(filePath: String) {
        val directory = Environment.DIRECTORY_DCIM + File.separator + momentsDirectoryName
        val resultFile = fileManager.saveMediaToPuplicFolder(Uri.parse(filePath), directory)
        addImageToGallery(resultFile?.path ?: return)
    }

    /**
     * Скачать медиа момента в папку с моментами
     */
    suspend fun downloadMomentAndAddToGallery(externalMediaUrl: String): File {
        return suspendCancellableCoroutine {
            Glide.with(context)
                .asFile()
                .load(externalMediaUrl)
                .listener(object : RequestListener<File> {
                    override fun onLoadFailed(
                        exception: GlideException?,
                        model: Any?,
                        target: Target<File>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        throw exception ?: UnknownError()
                    }

                    override fun onResourceReady(
                        resource: File?,
                        model: Any?,
                        target: Target<File>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource == null) {
                            throw UnknownError()
                        }
                        saveImageFile(
                            image = resource,
                            url = externalMediaUrl
                        )
                        it.resume(resource)
                        return false
                    }
                }).submit()
        }
    }

    private fun saveImageFile(image: File, url: String) {
        val imageFileName = url.substring(url.lastIndexOf('/') + 1)
        val imageFile = getOutputMediaFile(imageFileName) ?: return

        val outputStream = FileOutputStream(imageFile)
        val inputStream = FileInputStream(image)

        val inputFileChannel = inputStream.channel
        val outputFileChannel = outputStream.channel

        inputFileChannel.transferTo(0, inputFileChannel.size(), outputFileChannel)
        outputFileChannel.close()
        inputFileChannel.close()

        addImageToGallery(imageFile.path)

        Log.d(this.javaClass.simpleName, "Moment was saved successfully url: $url, image: $imageFile,")
    }

    private fun getOutputMediaFile(imageFileName: String): File {
        val directory = getMomentSaveDirectory()
        return File(directory.path + File.separator + imageFileName)
    }

    private fun getMomentSaveDirectory(): File {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            momentsDirectoryName
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    private fun addImageToGallery(filePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(filePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }
}
