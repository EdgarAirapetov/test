package com.numplates.nomera3.modules.comments.ui.fragment

import com.numplates.nomera3.R

enum class WhoDeleteComment(val stringResId: Int) {
    MODERATOR(R.string.comment_deleted_by_moderator),
    POST_AUTHOR(R.string.comment_deleted_by_author),
    COMMENT_AUTHOR(R.string.comment_deleted_by_comment_author),
    BOTH_POST_COMMENT_AUTHOR(R.string.comment_deleted_by_comment_author)
}
