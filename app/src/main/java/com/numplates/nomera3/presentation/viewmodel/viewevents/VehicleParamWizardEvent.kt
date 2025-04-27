package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class VehicleParamWizardEvent {
    object EmptyWizardEvent: VehicleParamWizardEvent()
    object WrongWizardPosition: VehicleParamWizardEvent()

}