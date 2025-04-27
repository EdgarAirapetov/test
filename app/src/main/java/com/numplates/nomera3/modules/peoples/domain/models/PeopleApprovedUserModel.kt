package com.numplates.nomera3.modules.peoples.domain.models

import com.meera.db.models.userprofile.UserSettingsFlags

data class PeopleApprovedUserModel(
    val userId: Long,
    val subscribersCount: Int,
    val userName: String,
    val accountType: Int,
    val approved: Int,
    val accountColor: Int,
    val topContentMaker: Int,
    val avatarSmall: String,
    val uniqueName: String,
    val settingsFlags: UserSettingsFlags?,
    val isUserSubscribed: Boolean,
    val hasMoments: Boolean? = null,
    val hasNewMoments: Boolean? = null,
    val posts: List<PeopleBloggerPostModel>
)
