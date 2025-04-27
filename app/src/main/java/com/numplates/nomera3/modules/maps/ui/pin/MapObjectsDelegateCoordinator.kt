package com.numplates.nomera3.modules.maps.ui.pin

import com.numplates.nomera3.modules.maps.ui.pin.model.FocusedItemHandler
import com.numplates.nomera3.modules.maps.ui.pin.model.MapObjectsConfigUiModel

interface MapObjectsDelegateCoordinator {

    fun getConfig(): MapObjectsConfigUiModel

    fun getFocusedItemHandler(): FocusedItemHandler

    fun getMarkerJobHandler(): MarkerJobHandler
}
