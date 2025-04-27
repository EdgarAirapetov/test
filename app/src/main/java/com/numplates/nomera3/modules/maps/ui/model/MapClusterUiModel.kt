package com.numplates.nomera3.modules.maps.ui.model

import com.google.android.gms.maps.model.LatLng

data class MapClusterUiModel(
    val id: Long,
    val latLng: LatLng,
    val capacity: String,
    val userAvatars: List<String>
)
