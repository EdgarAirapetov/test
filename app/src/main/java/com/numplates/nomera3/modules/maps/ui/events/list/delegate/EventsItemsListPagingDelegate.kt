package com.numplates.nomera3.modules.maps.ui.events.list.delegate

import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

class EventsItemsListPagingDelegate(
    scope: CoroutineScope,
    getNextPage: suspend (offset: Int, limit: Int) -> List<EventsListItem.EventItemUiModel>,
) : ItemsListPagingDelegate<EventsListItem.EventItemUiModel>(
    scope = scope,
    getNextPage = getNextPage
) {

    fun removeItem(postId: Long) = itemsFlow.update { itemsList ->
        itemsList.filter { (it as? EventsListItem.EventItemUiModel)?.postId != postId }
    }
}
