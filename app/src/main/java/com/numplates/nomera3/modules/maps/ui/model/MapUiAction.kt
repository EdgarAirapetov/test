package com.numplates.nomera3.modules.maps.ui.model

import android.os.Bundle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.SelectedEventsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.widget.model.MapTargetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel

sealed interface MapUiAction {
    data class CreateMyMarkerRequested(
        val location: LatLng? = null,
        val fallbackToDefaultLocation: Boolean = false
    ) : MapUiAction
    data class ShowFriendAndUserCityBoundsRequested(
        val friendLocation: LatLng,
        val cameraPosition: CameraPosition
    ) : MapUiAction
    data class SetCameraToUserCityLocationRequested(
        val fallbackToDefault: Boolean,
    ) : MapUiAction
    object FindMyLocationRequested : MapUiAction
    object GoogleMapInitialized : MapUiAction
    data class MapViewCreated(
        val arguments: Bundle?
    ) : MapUiAction
    data class MapUiValuesCalculated(
        val mapUiValues: MapUiValuesUiModel
    ) : MapUiAction
    object MainModeInitialized : MapUiAction
    data class AuxMapEventUpdated(
        val eventObject: EventObjectUiModel
    ) : MapUiAction
    data class RemoveMediaEvent(
        val model: UIAttachmentPostModel
    ) : MapUiAction
    data class MainMapEventUpdated(
        val eventObject: EventObjectUiModel
    ) : MapUiAction
    data class OnResumeCalled(val isMapOpenInTab: Boolean) : MapUiAction
    data class MapBottomSheetDialogStateChanged(val isOpen: Boolean) : MapUiAction
    data object MapDialogClosed : MapUiAction
    data class EnableEventsLayerDialogClosed(
        val enableLayerRequested: Boolean,
        val confirmAction: EnableEventsDialogConfirmAction
    ) : MapUiAction

    object FriendsListStubDialogClosed : MapUiAction

    object FriendsListPressed : MapUiAction

    sealed interface EventsListUiAction : MapUiAction {
        object EventsListPressed : EventsListUiAction
        object EventsListsClosed : EventsListUiAction
        data class EventsListItemSelected(val item: SelectedEventsListItemUiModel) : EventsListUiAction
        data class EventFiltersChanged(val eventFiltersUpdate: EventFiltersUpdateUiModel) : EventsListUiAction
        data class EventParticipationCategoryChanged(val participationCategoryIndex: Int) : EventsListUiAction
        data class SelectedPageChanged(val index: Int) : EventsListUiAction
        data class LoadNextListPageRequested(val type: EventsListType) : EventsListUiAction
        object CreateNewEvent : EventsListUiAction
        object ShowNearbyPage : EventsListUiAction
        object ShowNearbyPageWithRefresh : EventsListUiAction
        data class JoinEvent(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class LeaveEvent(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class ShowEventParticipants(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class ShowEventCreator(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class ShowEventHostProfile(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class NavigateToEvent(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        data class OpenEventPost(val eventItem: EventsListItem.EventItemUiModel) : EventsListUiAction
        object EventPostClosed : EventsListUiAction
        object EventsListItemDetailsCloseClicked : EventsListUiAction
        data class EventsListItemDeleted(val postId: Long) : EventsListUiAction
        data class CameraChanged(val latLng: LatLng) : EventsListUiAction
    }

    sealed interface MapWidgetPointInfoUiAction : MapUiAction {
        data class MapTargetChanged(val mapTarget: MapTargetUiModel) : MapWidgetPointInfoUiAction
        data object RefreshMapPoint : MapWidgetPointInfoUiAction
        data class WithMeeraLogo(val withLogo: Boolean) : MapWidgetPointInfoUiAction
        data class MapUiStateChanged(val change: PointInfoWidgetAllowedVisibilityChange) : MapWidgetPointInfoUiAction
    }

    sealed interface InnerUiAction {
        object AddEvent : InnerUiAction
        data class HandleMapUiAction(val action: MapUiAction) : InnerUiAction
    }

    sealed interface AnalyticsUiAction : MapUiAction {
        data class MapEventOnboardingAction(
            val onboardingType: AmplitudePropertyMapEventsOnboardingType,
            val actionType: AmplitudePropertyMapEventsOnboardingActionType
        ) : AnalyticsUiAction
        data class MapEventCreateTap(
            val where: AmplitudePropertyMapEventsCreateTapWhere
        ) : AnalyticsUiAction
        object MapEventLimitAlert : AnalyticsUiAction
        object RulesOpen : AnalyticsUiAction
        object EventSnippetOpenTap : AnalyticsUiAction
    }
}
