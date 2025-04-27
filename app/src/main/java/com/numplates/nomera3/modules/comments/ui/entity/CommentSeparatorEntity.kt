package com.numplates.nomera3.modules.comments.ui.entity

import com.numplates.nomera3.modules.comments.data.api.OrderType
import java.util.*

data class CommentSeparatorEntity(
        override val type: CommentViewHolderType = CommentViewHolderType.SEPARATOR,

        override val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
        val separatorType: CommentSeparatorType,
        var count: Int? = null,
        val immutable: Boolean = false,

        val data: SeparatorData,
) : CommentUIType {

    override val parentId: Long
        get() = data.parentId

}

data class SeparatorData(
        val parentId: Long,
        val targetCommentId: Long,
        val orderType: OrderType
)

enum class CommentSeparatorType {
    SHOW_MORE,
    HIDE_ALL
}
