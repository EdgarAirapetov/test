package com.numplates.nomera3.modules.maps.ui.widget.model

import com.numplates.nomera3.modules.maps.domain.widget.model.MapWidgetPointInfoModel

data class PointInfoExtendedUiModel(
    val pointInfo: MapWidgetPointInfoModel,
    val mapTarget: MapTargetUiModel,
    val isNetworkConnected: Boolean
)
