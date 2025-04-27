package com.numplates.nomera3.modules.comments.ui.util

import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse

//this - last comment id in adapter
fun Long.checkHasCommentIntersection(responseComments: List<CommentEntityResponse>): Boolean {
    responseComments.forEach {
        if (it.id == this) return true
    }

    return false
}
