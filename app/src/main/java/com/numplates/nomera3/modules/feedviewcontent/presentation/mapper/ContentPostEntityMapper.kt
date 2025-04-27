package com.numplates.nomera3.modules.feedviewcontent.presentation.mapper

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentGroupUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentItemUiModel

private const val LONG_IMAGE_ASPECT_THRESHOLD = 9f / 42f
private const val WIDE_IMAGE_ASPECT_THRESHOLD = 21f / 9f

fun PostUIEntity.toContentGroupUiModel(): ContentGroupUiModel {
    val item = this.toContentItemUiModel()
    return ContentGroupUiModel(
        id = 0,
        postId = postId,
        isEventPost = event != null,
        isPostSubscribed = isPostSubscribed,
        contentList = listOf(item)
    )
}

fun PostUIEntity.toContentItemUiModel(): ContentItemUiModel {
    val enableZoomToFit = getSingleAspect() < LONG_IMAGE_ASPECT_THRESHOLD
        || getSingleAspect() > WIDE_IMAGE_ASPECT_THRESHOLD
    return ContentItemUiModel(
        id = postId,
        user = user,
        contentUrl = getImageUrl(),
        postReactions = reactions,
        enableZoomToFit = enableZoomToFit,
        aspect = getSingleAspect()
    )
}
