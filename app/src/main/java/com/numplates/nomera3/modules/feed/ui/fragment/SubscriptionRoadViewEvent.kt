package com.numplates.nomera3.modules.feed.ui.fragment

sealed class SubscriptionRoadViewEvent {
    data class NeedRefresh(val expandAppBarLayout: Boolean = false) : SubscriptionRoadViewEvent()
}
