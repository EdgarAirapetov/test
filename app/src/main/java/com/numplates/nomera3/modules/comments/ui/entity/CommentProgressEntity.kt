package com.numplates.nomera3.modules.comments.ui.entity

import java.util.UUID

class CommentProgressEntity: CommentUIType {
    override val type: CommentViewHolderType = CommentViewHolderType.PROGRESS
    override val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
    override val parentId: Long? = null
}
