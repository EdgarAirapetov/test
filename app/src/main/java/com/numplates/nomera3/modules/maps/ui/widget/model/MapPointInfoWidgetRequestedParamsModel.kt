package com.numplates.nomera3.modules.maps.ui.widget.model

import com.google.android.gms.maps.model.LatLng

data class MapTargetUiModel(
    val latLng: LatLng,
    val zoom: Float
)
