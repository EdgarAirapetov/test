package com.numplates.nomera3.modules.userprofile.domain.model

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

data class AvatarModel(
    val animation: String?,
    val big: String,
    val id: Long,
    val main: Boolean,
    val post: PostUIEntity?,
    val postId: Long?,
    val small: String,
    val userId: Long,
    val isAdult: Boolean
)
