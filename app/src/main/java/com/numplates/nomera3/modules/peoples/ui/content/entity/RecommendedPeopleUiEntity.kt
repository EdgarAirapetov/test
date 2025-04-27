package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity

data class RecommendedPeopleUiEntity(
    val userId: Long,
    val userAvatarUrl: String,
    val userName: String,
    val userAge: Long?,
    val userCity: String,
    val fullUserAgeCity: String,
    val accountType: Int,
    val isAccountApproved: Boolean,
    val accountTypeEnum: AccountTypeEnum,
    val accountColor: Int,
    val mutualUsersEntity: MutualFriendsUiEntity,
    val totalMutualUsersCount: Int,
    val hasFriendRequest: Boolean,
    val subscriptionOn: Int,
    val isSubscribedToMe: Boolean,
    val friendStatus: Int,
    val isAllowToShowAge: Boolean,
    val topContentMaker: Boolean
)
