package com.numplates.nomera3.modules.bump.domain.entity

data class UserShakeModel(
    val userId: Long,
    val name: String,
    val uniqueName: String,
    val birthday: Long,
    val avatarSmall: String,
    val gender: Int,
    val accountType: Int,
    val accountColor: Int,
    val approved: Int,
    val topContentMaker: Int,
    val complete: Int,
    val cityId: Long,
    val city: String,
    val countryId: Long,
    val country: String,
    val isFriends: Int,
    val mutualUserModel: ShakeMutualUsersModel?
)
