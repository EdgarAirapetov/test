package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class MapEventsListsDelegateConfigUiModel(
    val uiEffectsFlow: MutableSharedFlow<MapUiEffect>,
    val innerUiActionFlow: MutableSharedFlow<MapUiAction.InnerUiAction>,
    val mapBottomSheetDialogIsOpenFlow: MutableStateFlow<Boolean>,
    val scope: CoroutineScope,
    val eventsListsYOffset: Int
)
