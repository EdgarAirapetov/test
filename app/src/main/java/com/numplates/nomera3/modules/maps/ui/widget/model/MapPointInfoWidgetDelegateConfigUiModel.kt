package com.numplates.nomera3.modules.maps.ui.widget.model

import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

class MapPointInfoWidgetDelegateConfigUiModel(
    val uiEffectsFlow: MutableSharedFlow<MapUiEffect>,
    val scope: CoroutineScope
)
