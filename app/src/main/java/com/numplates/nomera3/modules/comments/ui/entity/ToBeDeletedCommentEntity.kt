package com.numplates.nomera3.modules.comments.ui.entity

import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment

data class ToBeDeletedCommentEntity(
    val id: Long,
    val whoDeleteComment: WhoDeleteComment,
    var originalComment: CommentUIType
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ToBeDeletedCommentEntity) {
            id == other.id
        } else {
            false
        }
    }
}
