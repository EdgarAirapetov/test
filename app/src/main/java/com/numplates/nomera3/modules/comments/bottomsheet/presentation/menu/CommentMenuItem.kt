package com.numplates.nomera3.modules.comments.bottomsheet.presentation.menu

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment

sealed class CommentMenuItem(
    val titleResId: Int,
    val iconResId: Int,
    val iconAndTitleColor: Int = R.color.uiKitColorForegroundPrimary
) {
    data class ReplyToComment(val comment: CommentEntityResponse) :
        CommentMenuItem(R.string.reply_txt, R.drawable.ic_outlined_reply_m)

    data class CopyMessage(val comment: CommentEntityResponse) :
        CommentMenuItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m)

    data class DeleteComment(val commentId: Long, val whoDeleteComment: WhoDeleteComment?) :
        CommentMenuItem(
            R.string.road_delete, R.drawable.ic_outlined_delete_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        )

    data class AddComplaintForComment(val commentId: Long) :
        CommentMenuItem(R.string.comment_complain, R.drawable.ic_outline_attention_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        )

    data class BlockUser(val commentAuthorId: Long) :
        CommentMenuItem(R.string.settings_privacy_block_user, R.drawable.ic_outlined_circle_block_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong)
}
