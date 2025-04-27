package com.numplates.nomera3.modules.moments.show.domain

import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.meera.core.extensions.toJson
import com.meera.db.models.UploadItem
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.moments.show.data.UploadMomentWorker
import com.numplates.nomera3.modules.upload.data.UploadResult
import com.numplates.nomera3.modules.upload.data.moments.UploadMomentBundle
import com.numplates.nomera3.modules.upload.domain.usecase.UploadBaseUseCase
import com.numplates.nomera3.modules.upload.mapper.WorkInfoStateMapper
import com.numplates.nomera3.modules.upload.util.getState
import com.numplates.nomera3.modules.upload.util.waitWorkStateAsync
import kotlinx.coroutines.CoroutineScope
import java.util.UUID
import javax.inject.Inject

class MomentUploader(
    rootScope: CoroutineScope,
    uploadItem: UploadItem,
    updateStoreCallback: (UploadItem) -> Unit
) : UploadBaseUseCase(rootScope, uploadItem, updateStoreCallback) {

    private var uploadBundle: UploadMomentBundle = uploadItem.getUploadBundle(UploadMomentBundle::class.java)

    @Inject
    lateinit var workManager: WorkManager

    private val momentsBundleWorkerMapper = MomentBundleWorkerMapper()

    init {
        App.component.inject(this)
    }

    private val isWorkerFinish: (WorkInfo.State?) -> Boolean = { state ->
        state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED || state == null
    }

    override suspend fun execute(): UploadResult {
        if (uploadBundle.isAlreadyUploading()) {
            val operationId = uploadBundle.getUid()!!
            val currentState = workManager.getState(operationId)

            return if (isWorkerFinish(currentState)) {
                WorkInfoStateMapper.mapFinishState(currentState)
            } else {
                waitUpload(operationId)
            }
        } else {
            uploadBundle = runUploadWorkAndSaveOperationId(uploadBundle)

            saveUploadItem(
                uploadItem.copy(
                    uploadBundleStringify = uploadBundle.toJson()
                )
            )

            return waitUpload(uploadBundle.getUid()!!)
        }
    }

    private fun UploadMomentBundle.isAlreadyUploading(): Boolean {
        val isUidNull = getUid() == null
        val isStateFail = getUid()?.let { workManager.getState(it) == WorkInfo.State.FAILED } ?: false

        return isUidNull.not() && isStateFail.not()
    }

    private suspend fun waitUpload(operationId: UUID): UploadResult {
        val waitJob = rootScope.waitWorkStateAsync(workManager, operationId, isWorkerFinish)
        val resultState = waitJob.await()

        if (resultState == WorkInfo.State.SUCCEEDED) {
            onSuccess()
        }

        return WorkInfoStateMapper.mapFinishState(resultState)
    }

    private suspend fun onSuccess() = Unit

    private fun runUploadWorkAndSaveOperationId(uploadBundle: UploadMomentBundle): UploadMomentBundle {
        val constraints = Constraints.Builder()
            .build()

        val builder = momentsBundleWorkerMapper.map(uploadBundle)
        val inputData = builder.build()

        val addMomentWorkRequest = OneTimeWorkRequestBuilder<UploadMomentWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)

        val addMomentRequestBuilder = addMomentWorkRequest.build()
        val operationId = addMomentRequestBuilder.id

        uploadBundle.setUid(operationId)

        workManager.enqueue(addMomentRequestBuilder)

        return uploadBundle
    }

}
