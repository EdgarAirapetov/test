package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel

data class EventFiltersUpdateUiModel(
    val eventFilterType: EventFilterTypeUiModel,
    val eventFilterDate: EventFilterDateUiModel
)
