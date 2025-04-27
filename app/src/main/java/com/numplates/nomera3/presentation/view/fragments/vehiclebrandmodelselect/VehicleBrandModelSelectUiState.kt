package com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect

sealed class VehicleBrandModelSelectUiState {

    data object Loading : VehicleBrandModelSelectUiState()

    data class Success(val list: List<VehicleBrandModelItem>) : VehicleBrandModelSelectUiState()

}
