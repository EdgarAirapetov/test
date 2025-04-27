package com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase

sealed class MeeraVehicleEditState {
    data object OnUpdateVehicleSuccess : MeeraVehicleEditState()
    data object OnAddVehicleSuccess : MeeraVehicleEditState()
    data object OnLoading : MeeraVehicleEditState()
    data class MessageError(val message:String?) : MeeraVehicleEditState()
    data class NumPlateError(val message:String?) : MeeraVehicleEditState()
}
