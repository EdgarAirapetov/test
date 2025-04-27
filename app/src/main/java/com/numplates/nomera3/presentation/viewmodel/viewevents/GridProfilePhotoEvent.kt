package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class GridProfilePhotoEvent {
    object OnErrorSocket : GridProfilePhotoEvent()
    object OnCloseGalleryScreen: GridProfilePhotoEvent()
}