package com.numplates.nomera3.modules.maps.ui.events.list.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem

fun stubItemAdapterDelegate() =
    adapterDelegate<EventsListItem.StubItemUiModel, EventsListItem>(R.layout.item_events_list_stub) {}

