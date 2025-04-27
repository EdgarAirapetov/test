package com.numplates.nomera3.modules.places.ui.model

import com.numplates.nomera3.modules.places.domain.model.PlaceModel

sealed interface PlacesSearchEvent {
    data class PlaceSearched(val searchText: String) : PlacesSearchEvent
    object SearchCleared: PlacesSearchEvent
    data class PlaceSelected(val place: PlaceModel) : PlacesSearchEvent
    object Canceled : PlacesSearchEvent
}
