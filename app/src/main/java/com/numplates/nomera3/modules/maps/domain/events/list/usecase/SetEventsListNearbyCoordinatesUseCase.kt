package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import javax.inject.Inject

class SetEventListCoordinatesUseCase @Inject constructor(
    private val repository: MapEventsListsRepository
) {
    operator fun invoke(latLng: LatLng) =
        repository.setCoordinates(latLng)
}
