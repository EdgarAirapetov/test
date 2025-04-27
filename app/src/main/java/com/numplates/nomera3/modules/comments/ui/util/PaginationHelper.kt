package com.numplates.nomera3.modules.comments.ui.util

import com.numplates.nomera3.modules.reaction.data.ReactionType

class PaginationHelper {

    val hash: HashMap<Long, IntRange> = hashMapOf()

    var firstCommentId: Long? = null
    var lastCommentId: Long? = null

    var isTopPage = false
    var isLastPage = false

    var isLoadingAfterCallback: (Boolean) -> Unit = {}
    var isLoadingAfter = false

    var isLoadingBeforeCallback: (Boolean) -> Unit = {}
    var isLoadingBefore = false

    var needToShowReplyBtn = true

    var flyingReactionType: ReactionType? = null
    var flyingReactionCommentId: Long? = null

    fun clear() {
        hash.clear()

        firstCommentId = null
        lastCommentId = null

        isTopPage = false
        isLastPage = false
        isLoadingAfter = false
        isLoadingBefore = false

        needToShowReplyBtn = true
    }

}
