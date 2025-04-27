package com.numplates.nomera3.modules.reaction.ui.data

import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum

sealed class ReactionSource(val id: Long, open val reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {

    data class MomentComment(
        val momentId: Long,
        val commentUserId: Long,
        val momentUserId: Long,
        val commentId: Long
    ) : ReactionSource(commentId, ContentActionBar.ReactionHolderViewId.empty())

    data class Moment(
        val momentId: Long,
        override val reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
    ) : ReactionSource(momentId, reactionHolderViewId)

    data class PostComment(
        val postId: Long,
        val postUserId: Long?,
        val commentUserId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : ReactionSource(commentId, ContentActionBar.ReactionHolderViewId.empty())

    data class CommentBottomMenu(
        val postId: Long,
        val postUserId: Long?,
        val commentUserId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : ReactionSource(commentId, ContentActionBar.ReactionHolderViewId.empty())

    data class CommentBottomSheet(
        val postId: Long,
        val commentId: Long,
        val originEnum: DestinationOriginEnum?
    ) : ReactionSource(commentId, ContentActionBar.ReactionHolderViewId.empty())

    data class Post(
        override val reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
        val postId: Long,
        val originEnum: DestinationOriginEnum?
    ) : ReactionSource(postId, reactionHolderViewId)
}
