package com.numplates.nomera3.modules.upload.data.post

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.UploadBundle
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.feed.domain.mapper.toMediaEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.utils.getTrueTextWithProfanity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

import java.util.*

@Parcelize
data class UploadPostBundle(
    val text: String,
    val postId: Long?,
    val groupId: Int?,
    val imagePath: String?,
    val videoPath: String?,
    val mediaList: List<UploadMediaModel>?,
    val mediaAttachmentUriString: String?,
    val roadType: Int?,
    val whoCanComment: WhoCanCommentPostEnum?,
    val media: MediaEntity? = null,
    val event: EventEntity? = null,
    val backgroundId: Int? = null,
    val backgroundUrl: String? = null,
    val fontSize: Int?,
    val mediaPositioning: MediaPositioningDto?,
    val mediaChanged: Boolean = false
) : UploadBundle(), Parcelable {

    @IgnoredOnParcel
    @SerializedName("wasCompressed")
    var wasCompressed: Boolean = false

    fun hasNoCompressedMedia() : Boolean {
        if(mediaList.isNullOrEmpty()) return false
        mediaList.find { !it.mediaWasCompressed }?.let {
            return true
        }
        return false
    }
}

fun PostUIEntity.toUploadPostBundle() = UploadPostBundle(
    postId = postId,
    text = tagSpan?.getTrueTextWithProfanity() ?: postText,
    groupId = groupId?.toInt(),
    imagePath = getImageUrl(),
    videoPath = getVideoUrl(),
    mediaAttachmentUriString = null,
    roadType = if (privacy == PostPrivacy.PRIVATE) RoadSelectionEnum.MY.state else RoadSelectionEnum.MAIN.state,
    whoCanComment = WhoCanCommentPostEnum.getEnumFromStringValue(commentAvailability),
    media = media?.toMediaEntity(),
    backgroundId = backgroundId,
    backgroundUrl = backgroundUrl,
    fontSize = fontSize,
    event = null,
    mediaPositioning = null,
    mediaList = listOf()
)
