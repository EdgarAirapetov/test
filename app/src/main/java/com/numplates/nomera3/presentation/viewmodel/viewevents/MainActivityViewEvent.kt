package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class MainActivityViewEvent {

    object OnStartCallActivity : MainActivityViewEvent()

    object OnStopCallActivity : MainActivityViewEvent()

}