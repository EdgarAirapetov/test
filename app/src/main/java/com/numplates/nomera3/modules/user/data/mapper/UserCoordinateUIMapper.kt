package com.numplates.nomera3.modules.user.data.mapper

import com.numplates.nomera3.modules.user.domain.entity.UserCoordinateModel
import com.numplates.nomera3.modules.user.ui.entity.UserCoordinatesUIModel
import javax.inject.Inject

class UserCoordinateUIMapper @Inject constructor() {
    fun mapDomainToUI(domainModel: UserCoordinateModel): UserCoordinatesUIModel {
        return UserCoordinatesUIModel(
            lat = domainModel.lat,
            lng = domainModel.lng
        )
    }
}
