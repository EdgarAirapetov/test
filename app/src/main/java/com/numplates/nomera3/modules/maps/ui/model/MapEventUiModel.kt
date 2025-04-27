package com.numplates.nomera3.modules.maps.ui.model

import com.google.android.gms.maps.model.LatLng

data class MapEventUiModel(
    val id: Long,
    val latLng: LatLng,
    val title: String,
    val userAvatar: String
)
