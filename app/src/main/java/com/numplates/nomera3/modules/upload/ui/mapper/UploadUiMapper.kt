package com.numplates.nomera3.modules.upload.ui.mapper

import com.meera.db.models.UploadType
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.UploadState
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import com.numplates.nomera3.modules.upload.ui.model.QueueItem
import com.numplates.nomera3.modules.upload.ui.model.StatusToastAction
import com.numplates.nomera3.modules.upload.ui.model.StatusToastActionUiModel
import com.numplates.nomera3.modules.upload.ui.model.StatusToastState
import com.numplates.nomera3.modules.upload.ui.model.StatusToastUiModel
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UploadUiMapper @Inject constructor(app: App) {

    private val resources = app.resources

    fun mapUploadItem(uploadState: UploadState, availableEventCount: Int? = null): QueueItem<StatusToastUiModel> {
        val status = when (uploadState.status) {
            is UploadStatus.Failed -> StatusToastState.Error
            is UploadStatus.Processing -> StatusToastState.Progress(null)
            is UploadStatus.Success -> {
                val message = when (uploadState.uploadItem.type) {
                    UploadType.Post -> resources.getString(R.string.road_upload_post_success_text)
                    UploadType.EditPost -> resources.getString(R.string.road_upload_post_success_text)
                    UploadType.EventPost -> {
                        val eventPublishSuccess = resources.getString(R.string.road_upload_post_event_success_text)
                        if (availableEventCount != null && availableEventCount > 0) {
                            val availableEventsString = resources
                                .getQuantityString(R.plurals.map_events_available_count_plural, availableEventCount, availableEventCount)
                            "${eventPublishSuccess}\n$availableEventsString"
                        } else {
                            eventPublishSuccess
                        }
                    }
                    UploadType.Moment -> resources.getString(R.string.road_upload_moment_success_text)
                }
                StatusToastState.Success(message)
            }
        }
        val postBundle = (UploadBundleMapper.map(uploadState.uploadItem) as? UploadPostBundle)
        val action = when (uploadState.status) {
            is UploadStatus.Failed -> {
                StatusToastActionUiModel(
                    actionTitle = resources.getString(R.string.general_retry),
                    action = StatusToastAction.RetryUpload
                )
            }
            else -> null
        }
        val uiModel = StatusToastUiModel(
            state = status,
            imageUrl = postBundle?.mediaList?.firstOrNull()?.mediaUriPath,
            canPlayContent = postBundle?.videoPath != null || isAnimatedImage(postBundle?.imagePath),
            action = action
        )
        val minDuration = when (status) {
            is StatusToastState.Progress -> MIN_PROCESSING_DURATION_MS
            is StatusToastState.Success -> null
            else -> 0
        }
        val maxDuration = if (status is StatusToastState.Success) MAX_SUCCESS_DURATION_MS else null
        return QueueItem(
            payload = uiModel,
            minDuration = minDuration,
            maxDuration = maxDuration
        )
    }

    private fun isAnimatedImage(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val sample = File(path)
            val pos = sample.name.lastIndexOf('.')
            val ext = sample.name.substring(pos)
            ext.contains(".gif", ignoreCase = true)
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    companion object {
        private const val MIN_PROCESSING_DURATION_MS = 500L
        private const val MAX_SUCCESS_DURATION_MS = 3000L
    }
}
