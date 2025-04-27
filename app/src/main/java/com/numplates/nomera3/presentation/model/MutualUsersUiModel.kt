package com.numplates.nomera3.presentation.model

import androidx.annotation.ColorRes

data class MutualUsersUiModel(
    val mutualUsers: List<MutualUserUiModel>,
    @ColorRes val mutualUsersTextColorRes: Int,
    val mutualUsersText: String,
    val moreCount: Int
)
