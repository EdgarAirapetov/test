package com.numplates.nomera3.modules.maps.ui.model

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.friends.model.EnableFriendsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction

sealed interface MapUiEffect {
    data class CallUiStateChanged(val isVisible: Boolean) : MapUiEffect
    data class ShowEventTimePicker(val uiModel: TimePickerUiModel) : MapUiEffect
    data class ShowAddressSearch(val searchText: String) : MapUiEffect
    data class ShowEventsAbout(val eventsInfo: EventsInfoUiModel) : MapUiEffect
    object ShowEventConfigurationUi : MapUiEffect
    object ShowEventLimitReached : MapUiEffect
    object UpdateEventsOnMap : MapUiEffect
    object ShowWidget : MapUiEffect
    object HideWidget : MapUiEffect
    data class CreateMyMarker(
        val latLng: LatLng,
        val isShowMeOnMapEnabled: Boolean,
        val markerIsObsolete: Boolean,
        val checkCurrentLocationActive: Boolean,
        val moments: PinMomentsUiModel
    ) : MapUiEffect
    data class ShowFriendAndUserCityBounds(
        val friendLocation: LatLng,
        val userCityLocation: LatLng,
        val cameraPosition: CameraPosition
    ) : MapUiEffect
    data class UpdateCameraLocation(
        val location: LatLng,
        val zoom: Float,
        val yOffset: Int,
        val animate: Boolean,
        val isMyLocationActive: Boolean
    ) : MapUiEffect
    data class SetMyLocation(val location: LatLng) : MapUiEffect
    data class CalculateMapUiValues(val mapMode: MapMode) : MapUiEffect
    data class InitializeUi(val mapUiState: MapUiState) : MapUiEffect
    object UpdateMyMarker : MapUiEffect
    data class FocusMapItem(val focusedMapItem: FocusedMapItem?) : MapUiEffect
    data class ShowEnableEventsLayerDialog(val confirmAction: EnableEventsDialogConfirmAction) : MapUiEffect
    data class ShowEnableFriendsLayerDialog(val confirmAction: EnableFriendsDialogConfirmAction) : MapUiEffect
    object ShowEventsStubDialog : MapUiEffect
    object ShowCreateEventStubDialog : MapUiEffect
    object ShowMapControls : MapUiEffect
    object HideMapControls : MapUiEffect
    object ResetGlobalMap : MapUiEffect
    data class UpdateUserMarkerMoments(val updateModel: UserMomentsStateUpdateModel) : MapUiEffect
    object OpenEventsList : MapUiEffect
    object CloseEventsList : MapUiEffect
    object ShowFriendsListStub : MapUiEffect
    data class OpenEventNavigation(val eventPost: PostUIEntity) : MapUiEffect
    data class OpenEventParticipantsList(val eventPost: PostUIEntity) : MapUiEffect
    data class OpenUserProfile(val userId: Long) : MapUiEffect
    data class OpenEventCreatorAvatarProfile(val userId: Long) : MapUiEffect
    data class OpenEventsListItemDetails(val eventPost: PostUIEntity) : MapUiEffect
    object CloseEventsListItemDetails : MapUiEffect
    object OpenFriends : MapUiEffect
    data class SelectEventsListItem(val eventsListType: EventsListType, val item: EventsListItem) : MapUiEffect
    data class OpenUserProfileInSnippet(val userId: UserSnippetModel) : MapUiEffect
    data class SendMessage(val userId: Long) : MapUiEffect
    data class ShowErrorMessage(val message: Int) : MapUiEffect
}
