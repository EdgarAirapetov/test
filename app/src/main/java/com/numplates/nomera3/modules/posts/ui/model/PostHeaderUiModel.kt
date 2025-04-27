package com.numplates.nomera3.modules.posts.ui.model

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

data class PostHeaderUiModel(
    val post: PostUIEntity,
    val childPost: PostUIEntity?,
    val navigationMode: PostHeaderNavigationMode,
    val isOptionsAvailable: Boolean,
    val isCommunityHeaderEnabled: Boolean,
    val isLightNavigation: Boolean,
    val editInProgress: Boolean = false,
    val bigAvatar: Boolean = false
)

fun PostHeaderUiModel.isParentOfVipPost(): Boolean = childPost?.isVipPost() == true

fun PostHeaderUiModel.isParentPost(): Boolean = childPost != null
