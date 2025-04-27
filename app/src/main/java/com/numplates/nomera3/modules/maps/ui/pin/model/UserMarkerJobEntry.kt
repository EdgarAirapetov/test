package com.numplates.nomera3.modules.maps.ui.pin.model

import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import kotlinx.coroutines.Job

class UserMarkerJobEntry(
    val user: MapUserUiModel,
    val job: Job
)
