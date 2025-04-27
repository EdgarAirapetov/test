package com.numplates.nomera3.modules.reaction.ui.data

import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum

sealed class MeeraReactionSource(val id: Long, open val reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {

    data class MomentComment(
        val momentId: Long,
        val commentUserId: Long,
        val momentUserId: Long,
        val commentId: Long
    ) : MeeraReactionSource(commentId, MeeraContentActionBar.ReactionHolderViewId.empty())

    data class Moment(
        val momentId: Long,
        override val reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
    ) : MeeraReactionSource(momentId, reactionHolderViewId)

    data class PostComment(
        val postId: Long,
        val postUserId: Long?,
        val commentUserId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : MeeraReactionSource(commentId, MeeraContentActionBar.ReactionHolderViewId.empty())

    data class CommentBottomMenu(
        val postId: Long,
        val postUserId: Long?,
        val commentUserId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : MeeraReactionSource(commentId, MeeraContentActionBar.ReactionHolderViewId.empty())

    data class CommentBottomSheet(
        val postId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : MeeraReactionSource(commentId, MeeraContentActionBar.ReactionHolderViewId.empty())

    data class Post(
        override val reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        val postId: Long,
        val originEnum: DestinationOriginEnum?
    ) : MeeraReactionSource(postId, reactionHolderViewId)
}
