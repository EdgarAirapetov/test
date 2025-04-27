package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType
import com.numplates.nomera3.presentation.model.MutualUser

data class MutualSubscribersUiEntity(
    var mutualSubscribersFriends: List<MutualUser>,
    var moreCount: Int,
    var userType: AccountTypeEnum
) : UserUIEntity {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR

}
