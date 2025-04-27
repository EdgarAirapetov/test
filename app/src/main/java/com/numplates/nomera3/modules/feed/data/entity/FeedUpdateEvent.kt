package com.numplates.nomera3.modules.feed.data.entity

import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity

sealed class FeedUpdateEvent {

    /**
     * Обновляем через payload только не null поля.
     */
    class FeedUpdatePayload(
        val postId: Long? = null,
        val repostCount: Int? = null,
        val commentCount: Int? = null,
        val reactions: List<ReactionEntity>? = null,
        val moments: MomentInfoCarouselUiModel? = null
    ) : FeedUpdateEvent()

    class FeedUpdateMoments(
        val momentHolderId: Long,
        val roadType: RoadTypesEnum,
        val asPayload: Boolean,
        val moments: MomentInfoCarouselUiModel?,
        val scrollToGroupId: Long? = null,
        val scrollToStart: Boolean = false,
        val momentsBlockAvatar: String?,
        val momentsInfo: MomentInfoModel? = null
    ) : FeedUpdateEvent()

    /**
     * Обновляем через notifyItemChanged.
     */
    class FeedUpdateAll(val postId: Long) : FeedUpdateEvent()

    class FeedPostRemoved(val postId: Long) : FeedUpdateEvent()

    class FeedHideUserRoad(val userId: Long) : FeedUpdateEvent()

    class FeedPostSubscriptionChanged(
        val postId: Long,
        val isSubscribed: Boolean
    ) : FeedUpdateEvent()

    class FeedUserSubscriptionChanged(
        val postId: Long?,
        val userId: Long,
        val isSubscribed: Boolean,
        val needToHideFollowButton: Boolean,
        val isBlocked: Boolean
    ) : FeedUpdateEvent()

    class FeedUpdatePostComments(val postId: Long, val comments: List<CommentUIType>) : FeedUpdateEvent()
}
