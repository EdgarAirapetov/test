package com.numplates.nomera3.modules.maps.ui.pin.model

import com.numplates.nomera3.modules.maps.ui.model.MapClusterUiModel
import kotlinx.coroutines.Job

class ClusterMarkerJobEntry(
    val cluster: MapClusterUiModel,
    val job: Job
)
