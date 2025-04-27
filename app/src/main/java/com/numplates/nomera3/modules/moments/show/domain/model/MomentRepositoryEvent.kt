package com.numplates.nomera3.modules.moments.show.domain.model

import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import com.numplates.nomera3.modules.moments.show.domain.MomentsAction
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel

sealed class MomentRepositoryEvent {
    data class MomentActionBarStateUpdated(
        val updatedItem: MomentItemModel
    ) : MomentRepositoryEvent()

    data class MomentUserSubscriptionUpdated(
        val userId: Long, val isSubscribed: Boolean
    ) : MomentRepositoryEvent()

    data class MomentUserBlockStatusUpdated(
        val userId: Long, val isBlockedByMe: Boolean
    ) : MomentRepositoryEvent()

    data class UserMomentsStateUpdated(val action: MomentsAction, val userMomentsStateUpdate: UserMomentsStateUpdateModel) : MomentRepositoryEvent()

    data class ProfileUserMomentsStateUpdated(val userId: Long, val userMomentsModel: UserMomentsModel) : MomentRepositoryEvent()

    data class MomentsGroupsNewPageLoaded(val roadType: RoadTypesEnum, val moments: MomentInfoModel) : MomentRepositoryEvent()
}
