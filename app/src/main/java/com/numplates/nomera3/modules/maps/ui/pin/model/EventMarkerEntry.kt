package com.numplates.nomera3.modules.maps.ui.pin.model

import com.google.android.gms.maps.model.Marker
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel

data class EventMarkerEntry(
    val event: EventObjectUiModel,
    val isLarge: Boolean,
    val marker: Marker
)
