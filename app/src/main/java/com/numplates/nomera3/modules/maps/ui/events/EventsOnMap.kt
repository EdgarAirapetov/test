package com.numplates.nomera3.modules.maps.ui.events

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.events.model.AddEventButtonState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationUiMode
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetDataUiState
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapCameraState
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventConfigurationUiMode
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface EventsOnMap {
    val liveMapEvents: LiveData<List<EventObjectUiModel>>
    val livePlacesSearchState: LiveData<PlacesSearchUiState>
    val liveAuxEventSnippetDataUiState: LiveData<EventSnippetDataUiState>
    val liveEventSnippetDataUiState: LiveData<EventSnippetDataUiState>
    val uiEffectsFlow: Flow<MapUiEffect>
    val liveAddEventButtonState: LiveData<AddEventButtonState>
    var savedUploadPostBundle: UploadPostBundle?
    val liveEventConfigurationState: LiveData<EventConfigurationState>
    fun getInnerUiActionFlow() : Flow<MapUiAction.InnerUiAction>
    fun setEventSnippetState(snippetState: SnippetState)
    fun setupEventEditing(setupUiModel: EventEditingSetupUiModel)
    fun getEvents(visibleBounds: LatLngBounds)
    fun updateMainMapEvent(post: PostUIEntity)
    fun onSearchPlaces(searchText: String)
    fun onPlacesSearchCleared()
    fun onEventPlaceSelected(place: PlaceModel)
    fun onEventPublished()
    fun needToShowEventsOnboarding(): Boolean
    fun setEventsOnboardingShown(): Result<Unit>
    fun getAddress(latLng: LatLng)
    fun setMapCameraState(mapCameraState: MapCameraState)
    fun setMapOpenInTab(isMapOpenInTab: Boolean)
    fun setEventConfigurationUiMode(eventConfigurationUiMode: EventConfigurationUiMode)
    fun setEventConfigurationUiMode(eventConfigurationUiMode: MeeraEventConfigurationUiMode)

    fun setSelectedEventType(eventTypeItemUiModel: EventTypeItemUiModel)
    fun setSelectedEventDate(eventDateItemUiModel: EventDateItemUiModel)
    fun setSelectedTimeWithRes(time: LocalTime): LocalTime
    fun setSelectedTime(time: LocalTime)
    fun setMyLocationActive(isActive: Boolean)
    fun onSelectTime()
    fun onSelectAddress()
    fun onShowEventsAbout()
    fun getEventParameters(): EventParametersUiModel?
    fun addEvent()
    fun cancelAddEvent()
    fun setSelectedEvent(eventObject: EventObjectUiModel?)
    fun setSelectedEvent(event: PostUIEntity)

    fun getNextEventSnippetPage()
    fun setAuxMapEventSelected(eventObject: EventObjectUiModel?)
    fun updateAuxMapEvent(eventObject: EventObjectUiModel)
    fun getSettings(): Settings?
    fun clear()
    fun logMapEventSnippetOpen(openType: AmplitudePropertyMapSnippetOpenType)
    fun setMapEventSnippetCloseMethod(closeMethod: MapSnippetCloseMethod)
    fun logMapEventSnippetClosed()
    fun logMapEventOnboardingAction(
        onboardingType: AmplitudePropertyMapEventsOnboardingType,
        actionType: AmplitudePropertyMapEventsOnboardingActionType
    )

    fun onSelectedImage(pathPhoto: UIAttachmentPostModel)

    companion object {
        const val EVENT_SNIPPET_PAGE_SIZE = 10
    }
}
