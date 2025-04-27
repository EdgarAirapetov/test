package com.numplates.nomera3.modules.maps.ui

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetItem
import javax.inject.Inject

@AppScope
class MapParametersCache @Inject constructor() {

    private val postItemMap = mutableMapOf<Long, EventSnippetItem.EventPostItem>()

    fun putEventPostItem(eventPostItem: EventSnippetItem.EventPostItem) {
        postItemMap[eventPostItem.eventObject.eventPost.postId] = eventPostItem
    }

    fun getEventPostItem(postId: Long): EventSnippetItem.EventPostItem? {
        return postItemMap[postId]
    }

    fun clearEventObjects() {
        postItemMap.clear()
    }
}
