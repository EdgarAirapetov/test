package com.numplates.nomera3.modules.viewvideo.presentation.data

sealed interface ViewVideoHeaderEvent {
    object UserClicked : ViewVideoHeaderEvent
    object FollowClicked : ViewVideoHeaderEvent
    object UnfollowClicked : ViewVideoHeaderEvent
}
