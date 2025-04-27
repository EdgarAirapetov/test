package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.maps.domain.widget.model.GetMapWidgetPointInfoParamsModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPointInfoModel

interface MapWidgetRepository {
    suspend fun getMapWidgetPointInfo(params: GetMapWidgetPointInfoParamsModel): MapWidgetPointInfoModel
}
