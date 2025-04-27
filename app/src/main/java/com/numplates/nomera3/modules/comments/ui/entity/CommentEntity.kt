package com.numplates.nomera3.modules.comments.ui.entity

import com.meera.db.models.message.ParsedUniquename
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.reaction.data.ReactionType
import kotlinx.android.parcel.RawValue

data class CommentEntity(
        val comment: CommentEntityResponse,

        var isShowFull: Boolean = false,

        val tagSpan: @RawValue ParsedUniquename? = null,

        override val type: CommentViewHolderType = CommentViewHolderType.COMMENT,

        var needToShowReplyBtn: Boolean? = null,

        var birthdayTextRanges: List<IntRange>? = null,

        var flyingReactionType: ReactionType? = null
) : CommentUIType {

    override val id: Long
        get() = comment.id

    override val parentId: Long?
        get() = comment.parentId

}

interface CommentUIType {
    val id: Long
    val type: CommentViewHolderType
    val parentId: Long?
}

enum class CommentViewHolderType {
    COMMENT,
    SEPARATOR,
    DELETED_COMMENT,
    PROGRESS
}
