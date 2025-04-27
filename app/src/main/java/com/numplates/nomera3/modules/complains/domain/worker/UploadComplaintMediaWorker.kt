package com.numplates.nomera3.modules.complains.domain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.complains.domain.usecase.UploadComplaintsMediaUseCase
import timber.log.Timber
import javax.inject.Inject

class UploadComplaintMediaWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var uploadComplaintsMediaUseCase: UploadComplaintsMediaUseCase

    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        return try {
            uploadComplaintsMediaUseCase.invoke(
                complaintId = inputData.getInt(KEY_COMPLAINT_ID, -1),
                imagePath = inputData.getString(KEY_IMAGE_PATH),
                videoPath = inputData.getString(KEY_VIDEO_PATH),
            )
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }

    companion object {
        private const val KEY_COMPLAINT_ID = "key_complaint_id"
        private const val KEY_IMAGE_PATH = "key_image_path"
        private const val KEY_VIDEO_PATH = "key_video_path"

        fun obtainInputData(complaintId: Int, imagePath: String?, videoPath: String?): Data {
            return Data.Builder()
                .putInt(KEY_COMPLAINT_ID, complaintId)
                .putString(KEY_IMAGE_PATH, imagePath)
                .putString(KEY_VIDEO_PATH, videoPath)
                .build()
        }
    }
}
