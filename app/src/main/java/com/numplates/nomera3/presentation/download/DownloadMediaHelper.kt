package com.numplates.nomera3.presentation.download

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.upload.util.waitWorkStateAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val BUFFER_CAPACITY = 10

class DownloadMediaHelper @Inject constructor(private val application: App) {

    val downloadEvent = MutableSharedFlow<DownloadMediaEvent>(0, BUFFER_CAPACITY, BufferOverflow.DROP_OLDEST)

    private var downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun downloadVideoToGallery(postMediaDownloadType: PostMediaDownloadType) {
        val postId = postMediaDownloadType.postId

        if (havePostActiveWorker().not()) {
            downloadScope.launch {
                downloadPost(
                    scope = this,
                    postMediaDownloadType = postMediaDownloadType
                )
            }
        } else {
            downloadScope.launch {
                downloadEvent.emit(DownloadMediaEvent.PostAlreadyDownloading(postId))
            }
        }
    }

    private fun havePostActiveWorker(): Boolean {
        val workerList = WorkManager.getInstance(application.applicationContext)
            .getWorkInfosByTag(TAG_POST_VIDEO_GLOBAL)
            .get()
        workerList.forEach { postWorkInfo ->
            if (isWorkerFinish(postWorkInfo.state).not()) {
                return true
            }
        }

        return false
    }

    private suspend fun downloadPost(scope: CoroutineScope, postMediaDownloadType: PostMediaDownloadType) {
        val postId = postMediaDownloadType.postId
        val assetId = postMediaDownloadType.assetId

        generateLoadingEvent(postMediaDownloadType, MediaLoadingState.LOADING)

        val constraints = Constraints.Builder()
            .build()
        val inputData = Data.Builder()
            .putLong(DownloadVideoToGalleryWorker.POST_ID, postId)
            .putString(DownloadVideoToGalleryWorker.ASSET_ID, assetId)
            .build()

        val downloadVideoWorkRequest = OneTimeWorkRequestBuilder<DownloadVideoToGalleryWorker>()
            .addTag("$TAG_POST_VIDEO$postId")
            .addTag(TAG_POST_VIDEO_GLOBAL)
            .setConstraints(constraints)
            .setInputData(inputData)

        val downloadVideoWorkRequestBuilder = downloadVideoWorkRequest.build()
        val workManager = WorkManager.getInstance(application.applicationContext)
        workManager.enqueue(downloadVideoWorkRequestBuilder)

        val resultState = waitDownload(
            scope = scope,
            operationId = downloadVideoWorkRequestBuilder.id,
            workManager = workManager
        )

        when (resultState) {
            WorkInfo.State.SUCCEEDED -> {
                generateLoadingEvent(postMediaDownloadType, MediaLoadingState.SUCCESS)
            }
            WorkInfo.State.FAILED -> {
                generateLoadingEvent(postMediaDownloadType, MediaLoadingState.FAIL)
            }
            WorkInfo.State.CANCELLED -> {
                generateLoadingEvent(postMediaDownloadType, MediaLoadingState.CANCELED)
            }
            else -> {
                generateLoadingEvent(postMediaDownloadType, MediaLoadingState.FAIL)
            }
        }
    }

    private suspend fun generateLoadingEvent(postMediaDownloadType: PostMediaDownloadType, state: MediaLoadingState) {
        downloadEvent.emit(
            DownloadMediaEvent.PostDownloadState(
                postMediaDownloadType = postMediaDownloadType,
                state = state
            )
        )
    }

    private suspend fun waitDownload(
        scope: CoroutineScope,
        operationId: UUID,
        workManager: WorkManager
    ): WorkInfo.State {
        val waitJob = scope.waitWorkStateAsync(workManager, operationId, isWorkerFinish)
        val resultState = waitJob.await()

        return resultState ?: WorkInfo.State.FAILED
    }

    private val isWorkerFinish: (WorkInfo.State?) -> Boolean = { state ->
        state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED || state == null || state == WorkInfo.State.CANCELLED
    }

    fun stopDownloadingVideo(postId: Long) {
        WorkManager.getInstance(application.applicationContext).cancelAllWorkByTag(TAG_POST_VIDEO + postId)
    }

    companion object {
        const val TAG_POST_VIDEO = "postVideo"
        const val TAG_POST_VIDEO_GLOBAL = "postVideoGlobal"
    }

    /**
     * Обертка для postId, позволяет понять откуда загружается медиа поста – из дороги, детального просмотра поста или при повторной загрузки из тоста
     */
    sealed class PostMediaDownloadType(open val postId: Long, open val assetId: String?) {
        data class PostRoadDownload(override val postId: Long, override val assetId: String?)
            : PostMediaDownloadType(postId, assetId)
        data class PostDetailDownload(override val postId: Long, override val assetId: String?)
            : PostMediaDownloadType(postId, assetId)
        data class PostToastRetryDownload(override val postId: Long, override val assetId: String?)
            : PostMediaDownloadType(postId, assetId)

        /**
         * Требования продукта: чтобы загрузка на детальном просмотре поста не дублировалась в дороге (и наоборот)
         * Поэтому есть методы проверки на источник ниже
         */
        fun canShowOnRoad(): Boolean {
            return this is PostRoadDownload || this is PostToastRetryDownload
        }

        fun canShowOnPostDetail(): Boolean {
            return this is PostDetailDownload || this is PostToastRetryDownload
        }
    }
}


