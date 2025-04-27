package com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers

import com.numplates.nomera3.modules.reactionStatistics.domain.models.UserModel

data class ViewerModel(
    val reaction: String?,
    val user: UserModel,
    val viewedAt: Long
)
