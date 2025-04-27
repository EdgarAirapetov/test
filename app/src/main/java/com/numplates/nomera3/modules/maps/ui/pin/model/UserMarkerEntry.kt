package com.numplates.nomera3.modules.maps.ui.pin.model

import com.google.android.gms.maps.model.Marker
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel

data class UserMarkerEntry(
    val user: MapUserUiModel,
    val marker: Marker
)
