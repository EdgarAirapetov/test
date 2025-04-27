package com.numplates.nomera3.modules.viewvideo.presentation.data

import com.numplates.nomera3.modules.feed.ui.entity.UserPost

data class ViewVideoHeaderUiModel(
    val user: UserPost,
    val isSubscribedToUser: Boolean,
    val isShowFollowButton: Boolean
)
