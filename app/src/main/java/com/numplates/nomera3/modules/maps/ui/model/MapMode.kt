package com.numplates.nomera3.modules.maps.ui.model

import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel

sealed interface MapMode {
    object Main : MapMode
    sealed interface Aux : MapMode
    data class UserView(val user: MapUserUiModel, val isMe: Boolean) : Aux
    data class EventEditing(val eventEditingSetupUiModel: EventEditingSetupUiModel) : Aux
    data class EventView(val eventObject: EventObjectUiModel?) : Aux
}
