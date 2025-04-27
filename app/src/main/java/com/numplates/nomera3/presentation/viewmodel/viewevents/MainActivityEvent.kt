package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class MainActivityEvent {
    class ShowDialogEvent(var counter: Long): MainActivityEvent()
}