package com.numplates.nomera3.modules.upload.domain.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.toJson
import com.meera.db.models.UploadBundle
import com.meera.db.models.UploadItem
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.upload.data.UploadResult
import com.numplates.nomera3.modules.upload.domain.UploadState
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.usecase.UploadUseCaseFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val MAX_UPLOAD_TRIES = 2
private const val MAX_BUFFER_CAPACITY = 10

@AppScope
class UploadRepositoryImpl @Inject constructor(
    val dataStore: UploadDataStore,
    val useCaseFactory: UploadUseCaseFactory
) : UploadRepository {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val uploadState = MutableSharedFlow<UploadState>(1, MAX_BUFFER_CAPACITY, BufferOverflow.DROP_OLDEST)
    private var isUploading: Boolean = false

    init {
        initUploadModels()
    }

    override fun retryLastFailed() {
        coroutineScope.launch {
            uploadNextTopItem()
        }
    }

    override fun getState(): Flow<UploadState> {
        return uploadState
    }

    private fun initUploadModels() {
        coroutineScope.launch {
            uploadNextTopItem()
        }
    }

    private suspend fun uploadNextTopItem() {
        isUploading = true

        val uploadItem = dataStore.getDataStream().lastOrNull()
        if (uploadItem == null) {
            handleStackEmpty()
            return
        }

        try {
            increaseUploadTries(uploadItem)
            val useCase = useCaseFactory.getUseCase(coroutineScope, uploadItem)

            publishProcessing(uploadItem)


            val uploadResult = useCase.execute()

            if (uploadResult == UploadResult.Success) {
                publishSuccess(uploadItem)
            } else {
                checkMaxReachedFail(uploadItem)
                return
            }

            dataStore.removeItem(uploadItem)

            uploadNextTopItem()
        } catch (exception: Throwable) {
            Timber.e(exception)
            exception.printStackTrace()
            checkMaxReachedFail(uploadItem)
            return
        }

        handleStackEmpty()
    }

    private suspend fun checkMaxReachedFail(uploadItem: UploadItem) {
        if (uploadItem.uploadTries >= MAX_UPLOAD_TRIES) {
            handleMaxUploadTriesReached(uploadItem)
        } else {
            handleFail(uploadItem)
        }
    }

    private suspend fun handleFail(uploadItem: UploadItem) {
        isUploading = false
        publishFailed(uploadItem, false)
    }

    private fun handleStackEmpty() {
        isUploading = false
    }

    private suspend fun handleMaxUploadTriesReached(uploadItem: UploadItem) {
        isUploading = false
        dataStore.removeItem(uploadItem)
        publishFailed(uploadItem, true)
    }

    private suspend fun increaseUploadTries(uploadItem: UploadItem) {
        uploadItem.uploadTries++
        dataStore.addOrReplace(uploadItem)
    }

    private suspend fun publishProcessing(uploadItem: UploadItem) {
        publishState(
            status = UploadStatus.Processing,
            uploadItem = uploadItem
        )
    }

    private suspend fun publishSuccess(uploadItem: UploadItem) {
        publishState(
            status = UploadStatus.Success,
            uploadItem = uploadItem
        )
    }

    private suspend fun publishFailed(uploadItem: UploadItem, maxTriesReached: Boolean) {
        publishState(
            status = UploadStatus.Failed(maxTriesReached),
            uploadItem = uploadItem
        )
    }

    private suspend fun publishState(status: UploadStatus, uploadItem: UploadItem) {
        val state = UploadState(
            status = status,
            uploadItem = uploadItem
        )
        uploadState.emit(state)
    }

    override fun upload(type: UploadType, uploadBundle: UploadBundle): Job {
        return coroutineScope.launch {
            val uploadItem = UploadItem(
                type = type,
                uploadBundleStringify = uploadBundle.toJson(),
            )
            dataStore.addOrReplace(uploadItem)
            if (!isUploading) {
                uploadNextTopItem()
            }
        }
    }

    override fun isNowUploading(): Boolean {
        return isUploading
    }
}
