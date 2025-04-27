package com.numplates.nomera3.modules.maps.ui.view

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.model.MapUiValuesUiModel

interface MapUiController {
    fun updateCameraLocation(
        location: LatLng,
        zoom: Float? = null,
        yOffset: Int = 0,
        animate: Boolean = true,
        cancelCallback: (() -> Unit)? = null,
        callback: (() -> Unit)? = null
    )
    fun showMapControls()
    fun hideMapControls(showUserLocationView: Boolean = false, hideToolbar: Boolean = false)
    fun getMapUiValues(): MapUiValuesUiModel
    fun getTargetSnippetZoom(): Float
    fun updateEventsData()
    fun getMapMode(): MapMode?
    var isMapOpenInTab: Boolean

    fun focusMapItem(mapItem: FocusedMapItem?)
    fun updateEventMapItem(eventObject: EventObjectUiModel)
}
