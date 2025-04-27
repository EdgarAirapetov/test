package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class EditVehicleGarageViewEvent {
    object FailedToDeleteVehicle: EditVehicleGarageViewEvent()
    object VehicleDeleted: EditVehicleGarageViewEvent()
}