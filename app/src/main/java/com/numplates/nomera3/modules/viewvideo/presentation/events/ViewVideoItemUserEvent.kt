package com.numplates.nomera3.modules.viewvideo.presentation.events

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType

sealed class ViewVideoItemUserEvent {
    data class OnSubscribeToUserClicked(val userId: Long, val fromMenu: Boolean = false) : ViewVideoItemUserEvent()
    data class OnUnsubscribeFromUserClicked(val userId: Long) : ViewVideoItemUserEvent()
    data class OnBadWordClicked(val click: SpanDataClickType.ClickBadWord) : ViewVideoItemUserEvent()

    object OnOpenMenuClicked : ViewVideoItemUserEvent()
    data class OnDownloadVideoClicked(val postId: Long, val assetId: String?) : ViewVideoItemUserEvent()
    object OnCancelDownloadClicked : ViewVideoItemUserEvent()
    data class OnSubscribeToPost(val postId: Long) : ViewVideoItemUserEvent()
    data class OnUnsubscribeFromPost(val postId: Long) : ViewVideoItemUserEvent()
    data class OnHideUserRoad(val userId: Long) : ViewVideoItemUserEvent()
    data class AddComplaintPost(val postId: Long) : ViewVideoItemUserEvent()
    data class OnDeletePost(val postId: Long) : ViewVideoItemUserEvent()
    object OnRepostClick : ViewVideoItemUserEvent()
    data class OnCopyPostLink(val postId: Long) : ViewVideoItemUserEvent()
    data class OnRepostSuccess(val post: PostUIEntity, val repostTargetCount: Int = 1) : ViewVideoItemUserEvent()
    data class OnShareOutsideOpened(val isOpened: Boolean) : ViewVideoItemUserEvent()
    object OnPauseFragment : ViewVideoItemUserEvent()
}
