package com.numplates.nomera3.modules.moments.user.data.mapper

import com.meera.db.models.moments.UserMomentsPreviewDto
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsPreviewModel

object UserMomentsPreviewMapper {
    fun mapUserMomentsPreviewModel(userMomentsPreviewDto: UserMomentsPreviewDto): UserMomentsPreviewModel {
        return UserMomentsPreviewModel(
            id = userMomentsPreviewDto.id,
            url = userMomentsPreviewDto.url,
            viewed = userMomentsPreviewDto.viewed
        )
    }

    fun mapUserMomentsPreviewModel(momentItemDto: MomentItemDto): UserMomentsPreviewModel {
        return UserMomentsPreviewModel(
            id = momentItemDto.id,
            url = momentItemDto.asset?.preview ?: "",
            viewed = momentItemDto.viewed
        )
    }

    fun mapUserMomentsPreviewDto(model: UserMomentsPreviewModel): UserMomentsPreviewDto {
        return UserMomentsPreviewDto(
            id = model.id,
            url = model.url,
            viewed = model.viewed
        )
    }
}
