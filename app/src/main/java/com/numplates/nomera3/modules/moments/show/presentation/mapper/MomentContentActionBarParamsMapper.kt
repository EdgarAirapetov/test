package com.numplates.nomera3.modules.moments.show.presentation.mapper

import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel

fun MomentItemUiModel.toContentActionBarParams(isAuthed:Boolean, isAuthor: Boolean): ContentActionBar.Params {
    return ContentActionBar.Params(
        isEnabled = isInteractionAllowed(),
        reactions = reactions,
        viewsCount = viewsCount,
        userAccountType = userAccountType,
        commentCount = commentsCount,
        repostCount = repostsCount,
        isMoment = true,
        isMomentAuthor = isAuthor,
        commentsIsHide = if (isAuthed) isMomentCommentable().not() else false,
    )
}

fun MomentItemUiModel.toMeeraContentActionBarParams(isAuthed:Boolean, isAuthor: Boolean): MeeraContentActionBar.Params {
    return MeeraContentActionBar.Params(
        isEnabled = isInteractionAllowed(),
        reactions = reactions,
        viewsCount = viewsCount,
        userAccountType = userAccountType,
        commentCount = commentsCount,
        repostCount = repostsCount,
        isMoment = true,
        isMomentAuthor = isAuthor,
        commentsIsHide = if (isAuthed) isMomentCommentable().not() else false,
    )
}
