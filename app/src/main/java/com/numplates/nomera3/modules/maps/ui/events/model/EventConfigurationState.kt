package com.numplates.nomera3.modules.maps.ui.events.model

import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import java.time.LocalTime

sealed interface EventConfigurationState {
    data class Onboarding(
        val eventTypeItems: List<EventTypeItemUiModel>
    ) : EventConfigurationState
    object Closed : EventConfigurationState
    object Empty : EventConfigurationState
    data class Step2(
        val markerState: EventConfigurationMarkerState,
    ) : EventConfigurationState
    data class StepFirstConfiguration(
        val eventTypeItems: List<EventTypeItemUiModel>,
        val eventDateItems: List<EventDateItemUiModel>,
        val selectedTime: String,
        val markerState: EventConfigurationMarkerState,
        val isContinueEnabled: Boolean,
        val isHidden: Boolean,
        val isMyLocationActive: Boolean,
        val time: LocalTime
    ) : EventConfigurationState

    data object UploadingStarted : EventConfigurationState

    data class Configuration(
        val eventTypeItems: List<EventTypeItemUiModel>,
        val eventDateItems: List<EventDateItemUiModel>,
        val selectedTime: String,
        val markerState: EventConfigurationMarkerState,
        val isContinueEnabled: Boolean,
        val isHidden: Boolean,
        val isMyLocationActive: Boolean,
        val imageAttachment: UIAttachmentPostModel? = null
    ) : EventConfigurationState
}
