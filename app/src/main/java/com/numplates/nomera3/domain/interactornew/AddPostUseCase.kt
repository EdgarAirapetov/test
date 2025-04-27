package com.numplates.nomera3.domain.interactornew

import com.google.gson.Gson
import com.meera.core.extensions.toJson
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.data.network.PostCreationResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.data.api.UploadRoadApi
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

const val VIDEO_MP4_TYPE = "video/mp4"
const val IMAGE_TYPE = "image/*"
private const val FILE_NAME = "file"
const val TEXT_PLANE_NAME = "text/plain"

private const val DEFAULT_ID_VALUE = 0

class AddPostUseCase @Inject constructor(
    private val api: UploadRoadApi
) {

    suspend fun addPostV2(
        groupId: Int,
        text: String,
        imagePath: String?,
        videoPath: String?,
        roadType: Int?,
        whoCanComment: Int,
        media: MediaEntity? = null,
        event: EventEntity? = null,
        partialUploadId: String? = null,
        partialUploadIds: List<String>? = null,
        backgroundId: Int?,
        fontSize: Int?,
        mediaPositioning: String?,
        mediaPositioningList: HashMap<String, MediaPositioningDto>?
    ): ResponseWrapper<PostCreationResponse?>? {
        val groupIdBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), groupId.toString())
        val backgroundIdBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), backgroundId.toString())
        val fontSizeBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), fontSize.toString())
        val mediaPositioningBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), mediaPositioning.toString())
        val mediaPositioningListBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), Gson().toJson(mediaPositioningList))
        val textBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), text)
        val whoCanCommentBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), whoCanComment.toString())
        val roadTypeBody = RequestBody.create(TEXT_PLANE_NAME.toMediaTypeOrNull(), roadType.toString())
        val needRoadType = groupId == 0 && roadType != null
        val filePart: MultipartBody.Part?
        val partialUploadIdBody: RequestBody
        val partialUploadIdsBody: List<MultipartBody.Part>

        if (!partialUploadId.isNullOrEmpty()) {
            partialUploadIdBody = partialUploadId.toRequestBody(TEXT_PLANE_NAME.toMediaTypeOrNull())
            val resp = api.addPost(
                groupId = groupIdBody,
                text = textBody,
                uploadId = partialUploadIdBody,
                roadType = if (needRoadType) roadTypeBody else null,
                commentSetting = whoCanCommentBody,
                media = media,
                event = event,
                backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
                fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null,
                mediaPositioning = mediaPositioningBody
            )
            return resp
        }

        if (!partialUploadIds.isNullOrEmpty()) {
            partialUploadIdsBody = makeMultipartIdsBody(partialUploadIds)
            val resp = api.addPost(
                groupId = groupIdBody,
                text = textBody,
                uploadIds = partialUploadIdsBody,
                roadType = if (needRoadType) roadTypeBody else null,
                commentSetting = whoCanCommentBody,
                media = media,
                event = event,
                backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
                fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null,
                mediaPositioningList = mediaPositioningListBody
            )
            return resp
        }

        if (imagePath != null && imagePath.isNotEmpty()) {
            val file = File(imagePath)
            filePart = MultipartBody.Part.createFormData(
                FILE_NAME,
                file.name,
                RequestBody.create(IMAGE_TYPE.toMediaTypeOrNull(), file)
            )

            return api.addPost(
                groupId = groupIdBody,
                text = textBody,
                image = filePart,
                roadType = if (needRoadType) roadTypeBody else null,
                commentSetting = whoCanCommentBody,
                media = media,
                event = event,
                backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
                fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null,
                mediaPositioning = mediaPositioningBody
            )
        }

        if (videoPath != null && videoPath.isNotEmpty()) {
            val file = File(videoPath)
            filePart = MultipartBody.Part.createFormData(
                FILE_NAME,
                file.name,
                RequestBody.create(VIDEO_MP4_TYPE.toMediaTypeOrNull(), file)
            )

            return api.addPost(
                groupId = groupIdBody,
                text = textBody,
                image = filePart,
                roadType = if (needRoadType) roadTypeBody else null,
                commentSetting = whoCanCommentBody,
                media = media,
                event = event,
                backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
                fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null
            )
        }

        if (groupId == 0 && roadType != null) {
            return api.addPost(
                groupId = groupIdBody,
                text = textBody,
                roadType = roadTypeBody,
                commentSetting = whoCanCommentBody,
                media = media,
                event = event,
                backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
                fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null
            )
        }

        return api.addPost(
            groupId = groupIdBody,
            text = textBody,
            commentSetting = whoCanCommentBody,
            media = media,
            event = event,
            backgroundId = if (backgroundId != DEFAULT_ID_VALUE) backgroundIdBody else null,
            fontSize = if (fontSize != DEFAULT_ID_VALUE) fontSizeBody else null
        )
    }

    private fun makeMultipartIdsBody(partialUploadIds: List<String>): List<MultipartBody.Part> {
        return partialUploadIds.map {
            MultipartBody.Part.createFormData("upload_ids[]", it)
        }
    }
}
