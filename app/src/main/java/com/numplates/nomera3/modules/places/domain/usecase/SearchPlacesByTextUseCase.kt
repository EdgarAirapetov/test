package com.numplates.nomera3.modules.places.domain.usecase

import com.numplates.nomera3.modules.places.domain.PlacesRepository
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import javax.inject.Inject

class SearchPlacesByTextUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(text: String): List<PlaceModel> = repository.searchPlacesByText(text)
}
