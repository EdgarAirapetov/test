package com.numplates.nomera3.modules.maps.domain.widget.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapWidgetRepository
import com.numplates.nomera3.modules.maps.domain.widget.model.GetMapWidgetPointInfoParamsModel
import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPointInfoModel
import javax.inject.Inject

class GetMapWidgetPointInfoUseCase @Inject constructor(
    private val repository: MapWidgetRepository
) {
    suspend fun invoke(paramsModel: GetMapWidgetPointInfoParamsModel): MapWidgetPointInfoModel =
        repository.getMapWidgetPointInfo(paramsModel)
}
