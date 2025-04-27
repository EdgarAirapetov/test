package com.numplates.nomera3.modules.maps.ui.model

import com.google.android.gms.maps.model.LatLng

data class NearestFriendUiModel(
    val id: Long,
    val location: LatLng
)
