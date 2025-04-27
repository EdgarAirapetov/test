package com.numplates.nomera3.modules.viewvideo.presentation.events

sealed class ViewVideoItemEvent {
    object OnSubscribeToUserClicked : ViewVideoItemEvent()
    object OnUnsubscribeFromUserClicked : ViewVideoItemEvent()
}
