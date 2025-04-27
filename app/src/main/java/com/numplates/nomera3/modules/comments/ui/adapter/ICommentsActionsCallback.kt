package com.numplates.nomera3.modules.comments.ui.adapter

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity

interface ICommentsActionsCallback {

    fun onReactionBadgeClick(comment: CommentEntityResponse)

    fun onCommentLongClick(comment: CommentEntityResponse, position: Int)

    fun onCommentProfileClick(comment: CommentEntityResponse)

    fun onCommentReplyClick(comment: CommentEntityResponse)

    fun onCommentMention(userId: Long)

    fun onHashtagClicked(hashtag: String?)

    fun onCommentShowReactionBubble(
        commentId: Long,
        commentUserId: Long,
        showPoint: Point,
        viewsToHide: List<View>,
        reactionTip: TextView,
        currentReactionsList: List<ReactionEntity>,
        isMoveUpAnimationEnabled: Boolean,
    )

    fun onBirthdayTextClicked()

    fun onCommentDoubleClick(comment: CommentEntityResponse)

    fun onCommentPlayClickAnimation(commentId: Long)

    fun onCommentReactionAppearAnimation(reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>) = Unit

    fun onCommentLikeClick(comment: CommentEntityResponse)

    fun onCommentLinkClick(url: String?)
}
