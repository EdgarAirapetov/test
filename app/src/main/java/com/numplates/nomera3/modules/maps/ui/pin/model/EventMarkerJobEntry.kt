package com.numplates.nomera3.modules.maps.ui.pin.model

import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import kotlinx.coroutines.Job

class EventMarkerJobEntry(
    val event: EventObjectUiModel,
    val isLarge: Boolean,
    val job: Job
)
