package com.numplates.nomera3.modules.feedmediaview.ui.content.effect

import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feedmediaview.ui.ViewMultimediaMenuItems
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate

sealed class ViewMultimediaUiEffect {
    data class UpdatePostReaction(
        val reactionUpdate: MeeraReactionUpdate,
        val post: PostUIEntity
    ) : ViewMultimediaUiEffect()
    data class OpenShareMenu(val post: PostUIEntity) : ViewMultimediaUiEffect()
    data class ShowErrorMessage(val messageResId: Int) : ViewMultimediaUiEffect()

    data class OpenMenu(val menuItems: List<ViewMultimediaMenuItems>) : ViewMultimediaUiEffect()

    data class UpdateLoadingState(
        val loadingInfo: LoadingPostVideoInfoUIModel,
        val post: PostUIEntity
    ) : ViewMultimediaUiEffect()

    object SubscribedToPost : ViewMultimediaUiEffect()

    object UnsubscribedFromPost : ViewMultimediaUiEffect()

    object HiddenUserRoad : ViewMultimediaUiEffect()

    object AddedPostComplaint : ViewMultimediaUiEffect()

    object OnPostDeleted : ViewMultimediaUiEffect()

    object PostLinkCopied : ViewMultimediaUiEffect()
}
