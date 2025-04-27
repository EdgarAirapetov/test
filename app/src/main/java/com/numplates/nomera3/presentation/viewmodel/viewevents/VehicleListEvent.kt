package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel


sealed class VehicleListEvent {
    object VehicleError: VehicleListEvent()
    object VehicleClearAdapter: VehicleListEvent()
    data class VehicleUpdate(val vehicle: VehicleModel): VehicleListEvent()
}