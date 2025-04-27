package com.numplates.nomera3.presentation.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.work.WorkerParameters
import com.github.piasy.biv.utils.IOUtils
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.data.api.DownloadApi
import com.numplates.nomera3.presentation.upload.BaseMediaCoroutineWorker
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val VIDEO_FOLDER = "/video/"

class DownloadVideoToGalleryWorker(val appContext: Context, workerParams: WorkerParameters) :
    BaseMediaCoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var api: DownloadApi

    companion object {
        const val POST_ID = "post_id"
        const val ASSET_ID = "asset_id"
    }

    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        val postId: Long = inputData.getLong(POST_ID, 0L)
        val assetId: String? = inputData.getString(ASSET_ID)

        return try {
            val uri = downloadVideo(postId, assetId)
            if (uri != null) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }

    private suspend fun downloadVideo(postId: Long, assetId: String?): Uri? {
        return try {
            val responseBody = if (assetId == null) {
                api.downloadPostVideo(postId)
            } else {
                api.downloadPostVideoByAsset(postId, assetId)
            }
            saveVideoFile(postId, responseBody)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun getVideoSaveDirectory(): File {
        val directoryName = appContext.getString(R.string.app_name) + VIDEO_FOLDER
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            directoryName
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    private fun getOutputMediaFile(imageFileName: String): File {
        val directory = getVideoSaveDirectory()
        return File(directory.path + File.separator + imageFileName)
    }

    private fun saveVideoFile(postId: Long, body: ResponseBody): Uri {
        val videoFileName = "video_${postId}_${System.currentTimeMillis()}.mp4"
        val videoFile = getOutputMediaFile(videoFileName)

        val outputStream = FileOutputStream(videoFile)
        val inputStream = body.byteStream()

        IOUtils.copy(inputStream, outputStream)

        outputStream.close()
        inputStream.close()

        addImageToGallery(videoFile.path)

        return Uri.fromFile(videoFile)
    }

    private fun addImageToGallery(filePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(filePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        appContext.sendBroadcast(mediaScanIntent)
    }
}
