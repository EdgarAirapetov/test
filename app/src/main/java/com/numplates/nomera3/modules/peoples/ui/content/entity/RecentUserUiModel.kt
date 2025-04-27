package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class RecentUserUiModel(
    val uid: Long,
    val image: String?,
    val name: String?,
    val gender: Int,
    val accountType: AccountTypeEnum,
    val accountColor: Int,
    val approved: Boolean,
    val topContentMaker: Boolean,
    val hasMoments: Boolean,
    val hasNewMoments: Boolean
)
