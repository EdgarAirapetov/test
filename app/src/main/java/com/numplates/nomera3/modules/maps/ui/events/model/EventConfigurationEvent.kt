package com.numplates.nomera3.modules.maps.ui.events.model

import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum

sealed interface EventConfigurationEvent {
    object UiCloseInitiated : EventConfigurationEvent
    object MyLocationClicked : EventConfigurationEvent
    object CreateEventClicked : EventConfigurationEvent
    object ConfigurationFinished : EventConfigurationEvent
    data class MeeraConfigurationFinished(
        val title: String,
        val subtitle: String,
        val whoCanComment: WhoCanCommentPostEnum,
        val roadType: RoadSelectionEnum
    ) : EventConfigurationEvent
    object ConfigurationStep1Finished : EventConfigurationEvent
    object ConfigurationStep2Finished : EventConfigurationEvent
    data class EventTypeItemSelected(
        val eventTypeItemUiModel: EventTypeItemUiModel
    ) : EventConfigurationEvent
    data class EventDateItemSelected(
        val eventEventDateItemUiModel: EventDateItemUiModel
    ) : EventConfigurationEvent
    object RetryClicked : EventConfigurationEvent
    object SelectTimeClicked : EventConfigurationEvent
    object SearchPlaceClicked : EventConfigurationEvent
    object EventsAboutClicked : EventConfigurationEvent
    object RulesOpen : EventConfigurationEvent
}
