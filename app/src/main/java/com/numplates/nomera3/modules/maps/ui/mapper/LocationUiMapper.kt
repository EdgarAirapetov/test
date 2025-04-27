package com.numplates.nomera3.modules.maps.ui.mapper

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

interface LocationUiMapper {
    fun mapLatLng(model: CoordinatesModel): LatLng
}
