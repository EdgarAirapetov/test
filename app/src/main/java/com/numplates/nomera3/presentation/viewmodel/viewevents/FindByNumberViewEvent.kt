package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class FindByNumberViewEvent {

    object FailureGetVehicleTypes : FindByNumberViewEvent()

    object FailureGetCountries : FindByNumberViewEvent()

    object FailureSearchUser : FindByNumberViewEvent()

    object NotFoundSearchUser : FindByNumberViewEvent()

}