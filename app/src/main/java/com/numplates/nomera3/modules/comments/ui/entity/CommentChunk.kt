package com.numplates.nomera3.modules.comments.ui.entity

import com.numplates.nomera3.modules.comments.data.api.OrderType

data class CommentChunk(
        val items: List<CommentUIType>,

        // Maybe these vars should be removed from here
        val countBefore: Int,
        val countAfter: Int,

        // UIs
        val order: OrderType = OrderType.INITIALIZE,

        // It's only for comments which uses separators
        val separator: CommentSeparatorEntity? = null,

        val scrollCommentId: Long? = null
)
