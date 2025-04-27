package com.numplates.nomera3.presentation.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.App
import java.util.UUID
import java.util.concurrent.ExecutionException
import javax.inject.Inject


@AppScope
class UploadMediaHelper @Inject constructor(
        private val application: App
) : IUploadContract {

    override fun uploadImageToGallery(params: List<Uri>?): LiveData<WorkInfo>? {
        if (params.isNullOrEmpty()) return null
        val media = arrayListOf<String>()
        params.forEach {
            it.path?.let { strPath ->
                media.add(strPath)
            }
        }
        val constraints = Constraints.Builder()
                .build()
        val inputData = Data.Builder()
                .putStringArray(UploadImagesToGalleryWorker.MEDIA_URI_LIST, media.toTypedArray())
                .build()

        val uploadImageWorkRequest = OneTimeWorkRequestBuilder<UploadImagesToGalleryWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)


        val uploadImageWorkRequestBuilder = uploadImageWorkRequest.build()
        val operationId = uploadImageWorkRequestBuilder.id

        WorkManager.getInstance(application.applicationContext)
                .enqueue(uploadImageWorkRequestBuilder)
        return WorkManager.getInstance(application.applicationContext).getWorkInfoByIdLiveData(operationId)
    }

    override fun stopWorkMessageWithVideoToChat() {
        application.lastVideoOperation?.let { id ->
            WorkManager.getInstance(application.applicationContext).cancelWorkById(id)
        }
    }

    override fun checkIsLastWorkCompleted(): Boolean {
        if (application.lastVideoOperation == null) return true
        application.lastVideoOperation?.let {
            return !isWorkScheduled(it)
        }
        return true
    }

    private fun isWorkScheduled(uuid: UUID): Boolean {
        val instance = WorkManager.getInstance(application.applicationContext)
        val statuses = instance.getWorkInfoById(uuid)
        return try {
            var running = false
            val workInfoList = statuses.get()
            val state = workInfoList.state
            running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
            running
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }
}
