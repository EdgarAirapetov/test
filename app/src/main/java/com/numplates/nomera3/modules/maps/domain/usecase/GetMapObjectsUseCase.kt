package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.model.GetMapObjectsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapDataRepository
import javax.inject.Inject

class GetMapObjectsUseCase @Inject constructor(
    private val mapDataRepository: MapDataRepository
) {
    suspend fun invoke(params: GetMapObjectsParamsModel): MapObjectsModel {
        return mapDataRepository.getMapObjects(params)
    }
}
