package com.numplates.nomera3.modules.userprofile.data.mapper

import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import javax.inject.Inject

class ProfileSuggestionMapper @Inject constructor() {

    fun mapUserSimpleToModel(userSimple: UserSimple): ProfileSuggestionModel {
        return ProfileSuggestionModel(
            userId = userSimple.userId,
            avatarLink = userSimple.avatarSmall.orEmpty(),
            name = userSimple.name.orEmpty(),
            uniqueName = userSimple.uniqueName.orEmpty(),
            cityName = userSimple.city?.name.orEmpty(),
            isApproved = userSimple.approved.toBoolean(),
            isTopContentMaker = userSimple.topContentMaker.toBoolean(),
            accountType = createAccountTypeEnum(userSimple.accountType),
            mutualFriendsCount = userSimple.mutualFriendsCount ?: 0,
            gender = userSimple.gender
        )
    }

    fun mapUserSimpleDtoToModel(userSimple: UserSimpleDto): ProfileSuggestionModel {
        return ProfileSuggestionModel(
            userId = userSimple.userId,
            avatarLink = userSimple.avatarSmall.orEmpty(),
            name = userSimple.name.orEmpty(),
            uniqueName = userSimple.uniqueName.orEmpty(),
            cityName = userSimple.city?.name.orEmpty(),
            isApproved = userSimple.approved.toBoolean(),
            isTopContentMaker = userSimple.topContentMaker.toBoolean(),
            accountType = createAccountTypeEnum(userSimple.accountType),
            mutualFriendsCount = userSimple.mutualFriendsCount ?: 0,
            gender = userSimple.gender
        )
    }
}
