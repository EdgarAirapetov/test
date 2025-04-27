package com.numplates.nomera3.presentation.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.model.ImageMetadataModel
import com.meera.core.extensions.empty
import com.meera.core.utils.imagecompressor.Compressor
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.newroads.data.entities.jsonToEvent
import com.numplates.nomera3.modules.newroads.data.entities.jsonToMedia
import com.numplates.nomera3.modules.newroads.data.entities.jsonToMediaList
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Upload post with video
 */

private const val MAX_POST_IMAGE_WIDTH = 1200

class UploadPostWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var repository: PostsRepository

    @Inject
    lateinit var fileMetaDataDelegate: MediaFileMetaDataDelegate

    companion object {
        const val POST_ID = "POST_ID"
        const val POST_TEXT = "POST_TEXT"
        const val POST_IMAGE_PATH = "POST_IMAGE_PATH"
        const val POST_VIDEO_PATH = "POST_VIDEO_PATH"
        const val POST_GROUP_ID = "POST_GROUP_ID"
        const val POST_ROAD_TYPE = "POST_ROAD_TYPE"
        const val POST_WHO_CAN_COMMENT = "POST_WHO_CAN_COMMENT"
        const val POST_MEDIA_CHANGED = "POST_MEDIA_CHANGED"
        const val POST_MEDIA_DATA = "POST_MEDIA_DATA"
        const val POST_MEDIA_LIST_DATA = "POST_MEDIA_LIST_DATA"
        const val POST_EVENT_DATA = "POST_EVENT_DATA"
        const val POST_BACKGROUND_ID = "POST_BACKGROUND_ID"
        const val POST_FONT_SIZE = "POST_FONT_SIZE"
        const val POST_MEDIA_POSITIONING = "POST_MEDIA_POSITIONING"
        const val SEND_POST_TIMEOUT_SEC = 120 * 1000L
        const val GIF_EXT = ".gif"
    }

    init {
        App.component.inject(this)
    }

    @InternalCoroutinesApi
    override suspend fun doWork(): Result {
        val postId = inputData.getLong(POST_ID, 0L)
        val text = inputData.getString(POST_TEXT) ?: String.empty()
        val image = processImage(inputData.getString(POST_IMAGE_PATH))
        val video = inputData.getString(POST_VIDEO_PATH)
        val groupId = inputData.getInt(POST_GROUP_ID, 0)
        val roadType = inputData.getInt(POST_ROAD_TYPE, RoadSelectionEnum.MAIN.state)
        val whoCanComment = inputData.getInt(POST_WHO_CAN_COMMENT, WhoCanCommentPostEnum.EVERYONE.state)
        val media = inputData.getString(POST_MEDIA_DATA)?.jsonToMedia()
        val mediaList = inputData.getString(POST_MEDIA_LIST_DATA)?.jsonToMediaList()
        val event = inputData.getString(POST_EVENT_DATA)?.jsonToEvent()
        val backgroundId = inputData.getInt(POST_BACKGROUND_ID, 0)
        val fontSize = inputData.getInt(POST_FONT_SIZE, 0)
        val mediaPositioning = inputData.getString(POST_MEDIA_POSITIONING)
        val mediaChanged = inputData.getBoolean(POST_MEDIA_CHANGED,true)

        return uploadPostWithTimeout(
            postId = postId,
            text = text,
            groupId = groupId,
            image = image,
            video = video,
            roadType = roadType,
            whoCanComment = whoCanComment,
            media = media,
            mediaList = mediaList,
            event = event,
            backgroundId = backgroundId,
            fontSize = fontSize,
            mediaPositioning = mediaPositioning,
            mediaChanged = mediaChanged
        )
    }

    private suspend fun uploadPostWithTimeout(
        postId: Long, text: String,
        groupId: Int,
        image: String?,
        video: String?,
        roadType: Int,
        whoCanComment: Int,
        media: MediaEntity? = null,
        mediaList: List<UploadMediaModel>? = null,
        event: EventEntity? = null,
        backgroundId: Int,
        fontSize: Int,
        mediaPositioning: String?,
        mediaChanged: Boolean
    ): Result = withTimeout(1000 * SEND_POST_TIMEOUT_SEC) {

        if (postId != 0L) {
            return@withTimeout repository.editPost(
                postId = postId,
                text = text.trim(),
                imagePath = image,
                videoPath = video,
                media = media,
                mediaList = mediaList,
                backgroundId = backgroundId,
                fontSize = fontSize,
                mediaChanged = mediaChanged
            )
        } else {
            return@withTimeout repository.addNewPostV2(
                groupId = groupId,
                text = text.trim(),
                imagePath = image,
                videoPath = video,
                roadType = roadType,
                whoCanComment = whoCanComment,
                media = media,
                mediaList = mediaList,
                event = event,
                backgroundId = backgroundId,
                fontSize = fontSize,
                mediaPositioning = mediaPositioning
            )
        }
    }

    private suspend fun processImage(image: String?): String? {
        if (image == null || image.endsWith(GIF_EXT)) return image

        return try {
            fileMetaDataDelegate.getImageMetadata(image)?.let { metaData -> checkWidthAndCompressImage(metaData, image) } ?: image
        } catch (e: Exception) {
            Timber.e(e)
            image
        }
    }

    private suspend fun checkWidthAndCompressImage(
        metaData: ImageMetadataModel,
        image: String
    ): String = if (metaData.width > MAX_POST_IMAGE_WIDTH) {
        Compressor.compress(applicationContext, File(image)).path
    } else {
        image
    }
}


