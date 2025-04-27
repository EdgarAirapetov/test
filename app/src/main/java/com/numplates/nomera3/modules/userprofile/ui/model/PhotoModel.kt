package com.numplates.nomera3.modules.userprofile.ui.model

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

data class PhotoModel(
    val id: Long,
    val imageUrl: String,
    val animation: String? = null,
    val post: PostUIEntity?,
    val isAdult: Boolean,
    var showed: Boolean = false
)
