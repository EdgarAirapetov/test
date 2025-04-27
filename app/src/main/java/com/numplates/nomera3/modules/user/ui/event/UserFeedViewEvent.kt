package com.numplates.nomera3.modules.user.ui.event

sealed class UserFeedViewEvent {

    data class ScrollToPostPosition(
        val selectedPostPosition: Int,
        val scrollDelay: Long
    ) : UserFeedViewEvent()

    data class ScrollToFirstPostPositionUiEffect(
        val delayPlayVideo: Long
    ) : UserFeedViewEvent()
}
