package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.feed.data.api.EditPostApi
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.upload.data.post.EditedAssetModel
import javax.inject.Inject

class EditPostUseCase @Inject constructor(
    private val api: EditPostApi
) {
    suspend fun editPost(
        postId: Long,
        text: String,
        media: MediaEntity? = null,
        uploadId: String? = null,
        backgroundId: Int?,
        fontSize: Int?,
        mediaChanged: Boolean,
        uploadEditedIds: ArrayList<EditedAssetModel>?
    ): ResponseWrapper<PostEntityResponse> {

        val userType = "UserSimple"

        val requestBody = hashMapOf<String, Any?>().apply {
            if (mediaChanged) this["upload_id"] = uploadId
            this["text"] = text
            this["background_id"] = backgroundId
            this["font_size"] = fontSize
            if (uploadEditedIds != null) {
                this["assets"] = uploadEditedIds
            }
            this["media"] = media
        }

        return api.updatePost(
            postId = postId,
            body = requestBody,
            userType = userType
        )
    }
}
