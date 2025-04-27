package com.numplates.nomera3.modules.services.ui.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import javax.inject.Inject

class MeeraServicesUserProfileMapper @Inject constructor() {

    fun mapUserProfile(src: UserProfileModel): MeeraServicesUserUiModel {
        val storiesState = when {
            src.moments?.hasMoments ?: false && src.moments?.hasNewMoments ?: false -> UserpicStoriesStateEnum.NEW
            src.moments?.hasMoments ?: false -> UserpicStoriesStateEnum.VIEWED
            else -> UserpicStoriesStateEnum.NO_STORIES
        }
        return MeeraServicesUserUiModel(
            id = src.userId,
            userName = src.name ?: String.empty(),
            uniqueName = src.uniquename,
            avatarUrl = src.avatarSmall ?: String.empty(),
            storiesStateEnum = storiesState,
            approved = src.approved.toBoolean(),
            interestingAuthor = src.topContentMaker.toBoolean()
        )
    }

}
