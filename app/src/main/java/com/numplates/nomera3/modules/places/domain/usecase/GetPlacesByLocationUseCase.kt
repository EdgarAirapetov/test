package com.numplates.nomera3.modules.places.domain.usecase

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.places.domain.PlacesRepository
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import javax.inject.Inject

class GetPlacesByLocationUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(location: CoordinatesModel): List<PlaceModel> = repository
        .getPlacesByLocation(location)
}
