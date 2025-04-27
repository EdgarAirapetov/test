package com.numplates.nomera3.modules.user.data.mapper

import com.numplates.nomera3.data.newmessenger.response.ResponseMapUserState
import com.numplates.nomera3.modules.user.domain.entity.UserCoordinateModel
import javax.inject.Inject

class UserCoordinateMapper @Inject constructor() {
    fun mapDataToDomain(dataModel: ResponseMapUserState) =
        UserCoordinateModel(
            lat = dataModel.lat,
            lng = dataModel.lon
        )
}
