package com.numplates.nomera3.modules.maps.ui.events.model

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel

sealed interface EventSnippetItem {
    object LoaderItem : EventSnippetItem
    data class ErrorItem(
        val pinLocation: LatLng
    ) : EventSnippetItem
    data class EventPostItem(
        val eventObject: EventObjectUiModel,
        val updateWhenCreated: Boolean,
        val snippetHeight: Int
    ) : EventSnippetItem
}
