package com.numplates.nomera3.modules.services.ui.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import javax.inject.Inject

class MeeraServicesRecentsMapper @Inject constructor() {

    fun mapRecents(src: UserSimple): RecentUserUiModel {
        return RecentUserUiModel(
            uid = src.userId,
            image = src.avatarSmall ?: String.empty(),
            name = src.name ?: String.empty(),
            gender = 0,
            accountType = createAccountTypeEnum(src.accountType),
            accountColor = src.accountColor ?: 0,
            approved = src.approved.toBoolean(),
            topContentMaker = src.topContentMaker.toBoolean(),
            hasMoments = src.moments?.hasMoments.toBoolean(),
            hasNewMoments = src.moments?.hasNewMoments.toBoolean()
        )
    }

}
