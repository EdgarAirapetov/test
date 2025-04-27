package com.numplates.nomera3.modules.places.ui.model

sealed interface PlacesSearchUiState {
    object Default : PlacesSearchUiState
    data class Result(val places: List<PlaceItemUiModel>) : PlacesSearchUiState
    object NoResults : PlacesSearchUiState
    object Progress : PlacesSearchUiState
    object Error : PlacesSearchUiState
}
