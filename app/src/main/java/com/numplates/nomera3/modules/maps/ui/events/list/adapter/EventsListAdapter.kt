package com.numplates.nomera3.modules.maps.ui.events.list.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItemPayload
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction

class EventsListAdapter(
    itemActionListener: (MapUiAction.EventsListUiAction) -> Unit
) : AsyncListDifferDelegationAdapter<EventsListItem>(
    DiffCallback(),
    eventItemAdapterDelegate(itemActionListener),
    stubItemAdapterDelegate(),
    emptyItemAdapterDelegate(itemActionListener)
) {
    private class DiffCallback : DiffUtil.ItemCallback<EventsListItem>() {
        override fun areItemsTheSame(
            oldItem: EventsListItem,
            newItem: EventsListItem
        ): Boolean = oldItem.isTheSame(newItem)

        override fun areContentsTheSame(
            oldItem: EventsListItem,
            newItem: EventsListItem
        ): Boolean = oldItem.isContentTheSame(newItem)

        override fun getChangePayload(oldItem: EventsListItem, newItem: EventsListItem): Any? =
            when {
                oldItem is EventsListItem.EventItemUiModel && newItem is EventsListItem.EventItemUiModel ->
                    EventsListItemPayload.EventItemParticipation(newItem.participants)
                else -> null
            }
    }
}
