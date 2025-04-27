package com.numplates.nomera3.presentation.upload

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.UploadMediaToGalleryUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressImageForUploadUseCase
import timber.log.Timber
import javax.inject.Inject

class UploadImagesToGalleryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : BaseMediaCoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var getCropInfoUseCase: GetCropInfoUseCase

    @Inject
    lateinit var compressImageForUploadUseCase: CompressImageForUploadUseCase

    @Inject
    lateinit var uploadMedia: UploadMediaToGalleryUseCase


    companion object {
        const val MEDIA_URI_LIST = "media_uri_list"
    }
    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        val mediaData: Array<String>? = inputData.getStringArray(MEDIA_URI_LIST)
        val params = mutableListOf<Uri>()
        mediaData?.forEach {
            params.add(compressImage(it).let(Uri::parse))
        }
        return try {
            val imagesToUpload = convertImages(params)
            val result = uploadMedia.uploadMediaToGallery(imagesToUpload)
            deleteTempFiles(imagesToUpload, params)
            if(result.data != null)
                Result.success()
            else Result.failure()
        }catch (e: Exception){
            Timber.e(e)
            Result.failure()
        }

    }

    private suspend fun compressImage(imagePath: String): String =
        getCropInfoUseCase.invoke(
            fileType = fileManager.getMediaType(Uri.parse(imagePath)),
            mediaPlace = MediaControllerOpenPlace.Gallery
        )?.let {
            compressImageForUploadUseCase(
                imagePath = imagePath,
                cropInfo = it
            )
        } ?: imagePath

}
