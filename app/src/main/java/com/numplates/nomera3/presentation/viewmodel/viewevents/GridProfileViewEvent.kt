package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class GridProfileViewEvent {
    object OnPhotoLoadedSuccess: GridProfileViewEvent()
    object OnPhotoLoadedError: GridProfileViewEvent()
}