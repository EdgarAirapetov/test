package com.numplates.nomera3.modules.viewvideo.presentation.events

import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoMenuItems

sealed class ViewVideoItemUIEvent {
    data class UpdateVideoReaction(
        val reactionUpdate: MeeraReactionUpdate,
        val post: PostUIEntity
    ) : ViewVideoItemUIEvent()
    data class UpdateLoadingState(
        val loadingInfo: LoadingPostVideoInfoUIModel,
        val post: PostUIEntity
    ) : ViewVideoItemUIEvent()
    data class UpdatePostInfo(
        val post: PostUIEntity
    ): ViewVideoItemUIEvent()
    object OnPostDeleted : ViewVideoItemUIEvent()
    data class OpenMenu(val menuItems: List<ViewVideoMenuItems>) : ViewVideoItemUIEvent()
    data class OpenShareMenu(val post: PostUIEntity) : ViewVideoItemUIEvent()
    object SubscribedToPost : ViewVideoItemUIEvent()
    object UnsubscribedFromPost : ViewVideoItemUIEvent()
    object AddedPostComplaint : ViewVideoItemUIEvent()
    object HiddenUserRoad : ViewVideoItemUIEvent()
    object PostLinkCopied : ViewVideoItemUIEvent()
    object OnFragmentPaused : ViewVideoItemUIEvent()
    data class ShowErrorMessage(val messageResId: Int) : ViewVideoItemUIEvent()
}
