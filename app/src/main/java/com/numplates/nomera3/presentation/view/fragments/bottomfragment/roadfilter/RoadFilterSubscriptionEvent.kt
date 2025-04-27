package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

sealed class RoadFilterSubscriptionEvent {
    data class Error(val throwable: Throwable? = null) : RoadFilterSubscriptionEvent()
}