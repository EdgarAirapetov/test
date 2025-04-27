package com.numplates.nomera3.modules.comments.ui.entity

import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse

data class DeletedCommentEntity(
        val comment: CommentEntityResponse? = null,
        val stringResId: Int,
        override val type: CommentViewHolderType = CommentViewHolderType.DELETED_COMMENT
) : CommentUIType {

    override val id: Long = comment?.id ?: -1

    override val parentId: Long?
        get() = comment?.parentId

}
