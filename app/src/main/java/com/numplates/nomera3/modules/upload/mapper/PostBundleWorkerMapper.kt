package com.numplates.nomera3.modules.upload.mapper

import androidx.work.Data
import com.meera.core.extensions.toJson
import com.numplates.nomera3.modules.newroads.data.entities.toJson
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.upload.UploadPostWorker
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_BACKGROUND_ID
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_EVENT_DATA
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_FONT_SIZE
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_GROUP_ID
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_ID
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_IMAGE_PATH
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_MEDIA_CHANGED
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_MEDIA_DATA
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_MEDIA_LIST_DATA
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_MEDIA_POSITIONING
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_TEXT
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_VIDEO_PATH
import com.numplates.nomera3.presentation.upload.UploadPostWorker.Companion.POST_WHO_CAN_COMMENT

object PostBundleWorkerMapper {
    fun map(uploadBundle: UploadPostBundle): Data.Builder {
        return Data.Builder()
            .apply {
                putLong(POST_ID, uploadBundle.postId ?: 0)
                putString(POST_TEXT, uploadBundle.text.trim())
                putInt(POST_GROUP_ID, uploadBundle.groupId ?: 0)
                putString(POST_IMAGE_PATH, uploadBundle.imagePath)
                putString(POST_VIDEO_PATH, uploadBundle.videoPath)
                putInt(POST_WHO_CAN_COMMENT, uploadBundle.whoCanComment?.state ?: WhoCanCommentPostEnum.EVERYONE.state)
                putBoolean(POST_MEDIA_CHANGED, uploadBundle.mediaChanged)

                uploadBundle.media?.let { media ->
                    putString(POST_MEDIA_DATA, media.toJson())
                }
                uploadBundle.mediaList?.let { mediaList->
                    putString(POST_MEDIA_LIST_DATA, mediaList.toJson())
                }
                uploadBundle.event?.let { event ->
                    putString(POST_EVENT_DATA, event.toJson())
                }

                if (uploadBundle.groupId == null || uploadBundle.groupId == 0) {
                    putInt(UploadPostWorker.POST_ROAD_TYPE, uploadBundle.roadType ?: RoadSelectionEnum.MAIN.state)
                }

                uploadBundle.backgroundId?.let { backgroundId ->
                    putInt(POST_BACKGROUND_ID, backgroundId)
                }

                uploadBundle.fontSize?.let { fontSize ->
                    putInt(POST_FONT_SIZE, fontSize)
                }

                uploadBundle.mediaPositioning?.let { mediaPositioning ->
                    putString(POST_MEDIA_POSITIONING, mediaPositioning.toJson())
                }
            }
    }
}
