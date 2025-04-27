package com.numplates.nomera3.presentation.upload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.graphics.ExifUtils.setNormalOrientation
import com.numplates.nomera3.App
import com.numplates.nomera3.presentation.view.utils.NSupport
import timber.log.Timber
import java.io.File
import javax.inject.Inject

abstract class BaseMediaCoroutineWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var fileManager: FileManager

    init {
        App.component.inject(this)
    }

    /**
     * convert images before upload
     * */
    fun convertImages(listUri: List<Uri>): List<Uri>{
        val result = mutableListOf<Uri>()
        listUri.forEach { uri->
            result.add(convertImage(uri))
        }
        return result
    }


    /**
     * Sometimes backend is not accept some types of images and also
     * exist problems with rotation images on some device.
     * This method creates, new image file based on source image, to avoid problems
     *
     * @return return same uri while error
     * */
    private fun convertImage(uri: Uri): Uri{
        var extension = ""
        val index = uri.path?.lastIndexOf(".")
        if (index != null)
            extension = uri.path?.substring(index)?: ""
        if (extension.isEmpty()) return uri

        if (extension != ".gif") { //if file isnt gif
            var photoFile: File?
            var bitmap: Bitmap?
            try {
                photoFile = if (fileManager.isGooglePhoto(uri)) {
                    File(fileManager.saveImageFromGoogleDrives(uri))
                } else {
                    File(NSupport.getPath(applicationContext, uri))
                }
                val options = BitmapFactory.Options()
                bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                bitmap = null
                photoFile = null
            }
            return if (bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(setNormalOrientation(photoFile)) // 90
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                val temp = fileManager.createImageFile()
                val filePath = fileManager.saveBitmapInFile(bitmap, temp.absolutePath)
                Timber.d("Bazaleev: newImage = $filePath oldImage = ${uri.path}")
                Uri.parse(filePath)
            } else {
                uri
            }
        } else {
            return uri
        }

    }

    /**
     * Delete list of images
     * filesPath - array with modifyed images
     * originalFiles - files provided by user to upload - should not be deleted
     * */
    fun deleteTempFiles(filesPath: List<Uri?>, originalFiles: List<Uri?>){
        if (filesPath.size != originalFiles.size) return // lists size must be the same
        for (i in originalFiles.indices){
            if ((filesPath[i] != originalFiles[i])){ //delete only modifyed not null files
                val fileToDelete = filesPath[i]?.path
                if (!fileToDelete.isNullOrEmpty()) {
                    deleteTempFile(fileToDelete)
                    Timber.d("Bazaleev: newFile to delete = $fileToDelete")
                }
            }
        }
    }

    /**
     * Delete temp image after apload it to server
     * */
    private fun deleteTempFile(filePath: String){
        try {
            val extension = filePath.substring(filePath.lastIndexOf("."))
            Timber.d("Temp image file extension: $extension")
            if (extension != ".gif") {
                fileManager.deleteFile(filePath)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
