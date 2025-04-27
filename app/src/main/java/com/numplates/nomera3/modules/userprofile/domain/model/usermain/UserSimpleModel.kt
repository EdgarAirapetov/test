package com.numplates.nomera3.modules.userprofile.domain.model.usermain

import com.meera.db.models.moments.UserMomentsDto

data class UserSimpleModel(
    val userId: Long,
    val name: String? = "",
    val birthday: Long? = null,
    val avatarSmall: String? = "",
    val gender: Int? = -1,
    val accountType: Int? = 0,
    val accountColor: Int? = 0,
    val cityName: String?,
    val countryName: String?,
    val profileDeleted: Int? = 0,
    val profileBlocked: Int? = 0,
    val profileVerified: Int? = 0,
    val groupType: Int? = 0,
    val blacklistedMe: Int?,
    val blacklistedByMe: Int?,
    val uniqueName: String? = null,
    var isSystemAdministrator: Boolean = false,
    val approved: Int = 0,
    val topContentMaker: Int = 0,
    val mutualFriendsCount: Int? = null,
    var settingsFlags: UserSettingsFlagsModel?,
    val coordinates: LocationModel,
    val mainVehicle: UserVehicleModel?,
    val moments: UserMomentsDto? = null,
    val friendStatus: Int
)
