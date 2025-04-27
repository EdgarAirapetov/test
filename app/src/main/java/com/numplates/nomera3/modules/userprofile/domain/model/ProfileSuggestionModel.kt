package com.numplates.nomera3.modules.userprofile.domain.model

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class ProfileSuggestionModel(
    val userId: Long,
    val avatarLink: String,
    val name: String,
    val uniqueName: String,
    val cityName: String,
    val isApproved: Boolean,
    val isTopContentMaker: Boolean,
    val accountType: AccountTypeEnum,
    val mutualFriendsCount: Int,
    val gender: Int?
)
