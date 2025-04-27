package com.numplates.nomera3.modules.peoples.domain.models

import com.meera.db.models.userprofile.UserSettingsFlags
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity

data class PeopleRelatedUserModel(
    val userId: Long,
    val name: String,
    val accountColor: Int,
    val accountType: Int,
    val approved: Int,
    val avatar: String,
    val birthday: Long?,
    val cityId: Long,
    val cityName: String,
    val countryId: Long,
    val countryName: String,
    val gender: Int,
    val settingsFlags: UserSettingsFlags?,
    // TODO: BR-20197 Пока будет такая модель для общих подписок. Сделать другую, когда ветка PO-533 будет в dev
    val mutualFriends: MutualFriendsUiEntity?,
    val mutualTotalCount: Int,
    val hasFriendRequest: Boolean,
    val topContentMaker: Boolean
)
