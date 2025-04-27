package com.numplates.nomera3.modules.maps.ui.events.list.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemEventsListEmptyBinding
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction

fun emptyItemAdapterDelegate(itemActionListener: (MapUiAction.EventsListUiAction) -> Unit) =
    adapterDelegate<EventsListItem.EmptyItemUiModel, EventsListItem>(R.layout.item_events_list_empty) {
        val binding = ItemEventsListEmptyBinding.bind(itemView)
        binding.elewItemEventsListEmpty.uiActionListener = itemActionListener
        bind {
            binding.elewItemEventsListEmpty.setUiModel(item.uiModel)
        }
    }

