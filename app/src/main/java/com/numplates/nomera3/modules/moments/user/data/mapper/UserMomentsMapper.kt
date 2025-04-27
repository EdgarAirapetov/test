package com.numplates.nomera3.modules.moments.user.data.mapper

import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsSimpleModel

object UserMomentsMapper {
    fun mapUserMomentsModel(userMomentsDto: UserMomentsDto): UserMomentsModel {
        return UserMomentsModel(
            hasMoments = userMomentsDto.hasMoments?.toBoolean() ?: false,
            hasNewMoments = userMomentsDto.hasNewMoments?.toBoolean() ?: false,
            countNew = userMomentsDto.countNew ?: 0,
            countTotal = userMomentsDto.countTotal ?: 0,
            previews = userMomentsDto.previews?.map { UserMomentsPreviewMapper.mapUserMomentsPreviewModel(it) } ?: arrayListOf()
        )
    }

    fun mapUserMomentsSimpleModel(userMomentsDto: UserMomentsDto): UserMomentsSimpleModel {
        return UserMomentsSimpleModel(
            hasMoments = userMomentsDto.hasMoments?.toBoolean() ?: false,
            hasNewMoments = userMomentsDto.hasNewMoments?.toBoolean() ?: false
        )
    }

    fun mapUserMomentsDto(model: UserMomentsModel?): UserMomentsDto {
        return UserMomentsDto(
            hasMoments = model?.hasMoments.toInt(),
            hasNewMoments = model?.hasNewMoments.toInt(),
            countNew = model?.countNew,
            countTotal = model?.countTotal,
            previews =  model?.previews?.map { UserMomentsPreviewMapper.mapUserMomentsPreviewDto(it) }
        )
    }
}
