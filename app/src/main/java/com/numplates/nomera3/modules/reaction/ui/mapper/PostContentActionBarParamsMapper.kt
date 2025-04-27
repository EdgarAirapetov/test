package com.numplates.nomera3.modules.reaction.ui.mapper

import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

//fun Post.toContentActionBarParams(): ContentActionBar.Params {
//    val isEnabled = (deleted == 1 || postHidden == 1).not()
//    return ContentActionBar.Params(
//        isEnabled = isEnabled,
//        reactions = reactions ?: emptyList(),
//        userAccountType = user?.accountType,
//        commentCount = commentsCount,
//        repostCount = repostsCount,
//        isMoment = false,
//        isCommentButtonEnable = true
//    )
//}

fun PostUIEntity.toContentActionBarParams(): ContentActionBar.Params {
    return ContentActionBar.Params(
        isEnabled = true,
        reactions = reactions ?: emptyList(),
        userAccountType = user?.accountType?.value,
        commentCount = commentCount,
        repostCount = repostCount,
        isMoment = false,
        commentsIsHide = false,
        isPrivateGroupPost = isPrivateGroupPost
    )
}

fun PostUIEntity.toMeeraContentActionBarParams(): MeeraContentActionBar.Params {
    return MeeraContentActionBar.Params(
        isEnabled = true,
        reactions = reactions ?: emptyList(),
        userAccountType = user?.accountType?.value,
        commentCount = commentCount,
        repostCount = repostCount,
        isMoment = false,
        commentsIsHide = false,
        isPrivateGroupPost = isPrivateGroupPost
    )
}

