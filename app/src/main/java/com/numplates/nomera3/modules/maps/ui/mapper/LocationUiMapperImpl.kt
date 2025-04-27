package com.numplates.nomera3.modules.maps.ui.mapper

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import javax.inject.Inject

class LocationUiMapperImpl @Inject constructor(): LocationUiMapper {
    override fun mapLatLng(model: CoordinatesModel): LatLng = LatLng(model.lat, model.lon)
}
