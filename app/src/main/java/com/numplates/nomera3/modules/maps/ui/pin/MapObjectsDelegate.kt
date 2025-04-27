package com.numplates.nomera3.modules.maps.ui.pin

import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapClusterUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.EventMarkerEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.FocusedItemHandler
import com.numplates.nomera3.modules.maps.ui.pin.model.MapObjectsConfigUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.UserMarkerEntry
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.presentation.viewmodel.MapViewModel

class MapObjectsDelegate(
    private val fragment: Fragment,
    private val map: GoogleMap,
    private val mapViewModel: MapViewModel
) : MapObjectsDelegateCoordinator {

    var objectsDisabled = false

    private val userMarkerSize = (DEFAULT_USER_MARKER_SIZE_PX * fragment.resources.displayMetrics.density).toInt()

    private val markerJobHandler = MarkerJobHandler(fragment)
    private val mapClusterObjectsDelegate = MapClusterObjectsDelegate(
        fragment = fragment,
        map = map,
        coordinator = this
    )
    private val mapEventObjectsDelegate = MapEventObjectsDelegate(
        fragment = fragment,
        map = map,
        coordinator = this
    )
    private val mapUserObjectsDelegate = MapUserObjectsDelegate(
        fragment = fragment,
        map = map,
        coordinator = this
    )
    private val focusItemHandler = FocusedItemHandler(
        onDefocus = { item ->
            when (item) {
                is FocusedMapItem.User -> mapUserObjectsDelegate.setUserFocus(userId = item.userId, focused = false)
                is FocusedMapItem.Event -> mapEventObjectsDelegate.setEventFocus(eventObject = item.eventObject, focused = false)
                null -> Unit
            }
        },
        onFocus = { item ->
            when (item) {
                is FocusedMapItem.User -> mapUserObjectsDelegate.setUserFocus(userId = item.userId, focused = true)
                is FocusedMapItem.Event -> mapEventObjectsDelegate.setEventFocus(eventObject = item.eventObject, focused = true)
                null -> Unit
            }
        }
    )

    override fun getConfig(): MapObjectsConfigUiModel = MapObjectsConfigUiModel(
        userMarkerSize = userMarkerSize,
        userUid = mapViewModel.getUserUid(),
        objectsDisabled = objectsDisabled
    )

    override fun getFocusedItemHandler(): FocusedItemHandler = focusItemHandler

    override fun getMarkerJobHandler(): MarkerJobHandler = markerJobHandler

    fun focusMapItem(mapItem: FocusedMapItem?) = focusItemHandler.focusMapItem(mapItem)

    fun handleClusters(clusters: List<MapClusterUiModel>) = mapClusterObjectsDelegate.handleClusters(clusters)

    fun updateEventObject(eventObject: EventObjectUiModel) {
        focusItemHandler.updateFocusedEvent(eventObject)
        mapEventObjectsDelegate.updateEventObject(eventObject)
    }

    fun findEventMarkerEntry(markerId: String): EventMarkerEntry? =
        mapEventObjectsDelegate.findEventMarkerEntry(markerId)

    fun handleEvents(events: List<EventObjectUiModel>) = mapEventObjectsDelegate.handleEvents(events)

    fun clearEventMarkers() = mapEventObjectsDelegate.clearEventMarkers()

    fun handleUsers(users: List<MapUserUiModel>) = mapUserObjectsDelegate.handleUsers(users)

    fun addUserMapObject(user: MapUserUiModel) = mapUserObjectsDelegate.addUserMapObject(user)

    fun findUserMarkerEntry(markerId: String): UserMarkerEntry? = mapUserObjectsDelegate.findUserMarkerEntry(markerId)

    fun removeOwnMarkerFromList() = mapUserObjectsDelegate.removeOwnMarkerFromList()

    fun updateUserMarkerMoments(updateModel: UserMomentsStateUpdateModel) =
        mapUserObjectsDelegate.updateUserMarkerMoments(updateModel)

    fun clearMarkers() {
        mapUserObjectsDelegate.clearUserMarkers()
        mapClusterObjectsDelegate.clearClusterMarkers()
        mapEventObjectsDelegate.clearEventMarkers()
    }

    fun isEventsVisible(): Boolean =
        mapViewModel.mapSettings.showPeople.not() || map.cameraPosition.zoom >= MIN_EVENT_ZOOM

    companion object {
        private const val DEFAULT_USER_MARKER_SIZE_PX = 64

        const val MAP_USERS_ZINDEX = 2.0f
        const val MAP_CLUSTERS_ZINDEX = 3.0f
        const val MAP_EVENTS_ZINDEX = 4.0f
        const val MAP_FRIENDS_ZINDEX = 5.0f
        const val MAP_MY_ZINDEX = 6.0f
        const val MAP_FOCUSED_PIN_ZINDEX = 7.0f

        private const val MIN_EVENT_ZOOM = 3
    }
}
