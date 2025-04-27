package com.numplates.nomera3.modules.maps.ui.pin.model

import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.eventPostId

class FocusedItemHandler(
    private val onDefocus: (FocusedMapItem?) -> Unit,
    private val onFocus: (FocusedMapItem?) -> Unit,
) {
    private var focusedMapItem: FocusedMapItem? = null

    fun focusMapItem(mapItem: FocusedMapItem?) {
        onDefocus.invoke(focusedMapItem)
        onFocus.invoke(mapItem)
        focusedMapItem = mapItem
    }

    fun getFocusedItem(): FocusedMapItem? = focusedMapItem

    fun updateFocusedEvent(eventObject: EventObjectUiModel) {
        if (focusedMapItem.eventPostId() == eventObject.eventPost.postId) {
            focusedMapItem = (focusedMapItem as? FocusedMapItem.Event)?.copy(eventObject = eventObject)
        }
    }
}
