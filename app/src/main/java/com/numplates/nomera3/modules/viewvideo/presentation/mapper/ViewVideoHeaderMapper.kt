package com.numplates.nomera3.modules.viewvideo.presentation.mapper

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoHeaderUiModel

fun PostUIEntity.toViewVideoHeader(): ViewVideoHeaderUiModel? {
    val user = this.user ?: return null
    return ViewVideoHeaderUiModel(
        user = user,
        isSubscribedToUser = user.subscriptionOn.toBoolean(),
        isShowFollowButton = needToShowFollowButton
    )
}
