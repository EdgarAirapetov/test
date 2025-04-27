package com.numplates.nomera3.modules.userprofile.domain.model

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

data class GalleryItemModel(
    val createdAt: Long,
    val id: Long,
    val link: String,
    val post: PostUIEntity?,
    val postId: Long?,
    val isAdult:Boolean
)
