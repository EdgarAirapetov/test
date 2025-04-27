package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.google.android.gms.maps.model.LatLng
import com.meera.db.models.message.ParsedUniquename
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventStatusUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel

sealed interface EventsListItem {

    fun isTheSame(other: EventsListItem): Boolean
    fun isContentTheSame(other: EventsListItem): Boolean

    data class EventItemUiModel(
        val eventId: Long,
        val postId: Long,
        val location: LatLng,
        val eventLabel: EventLabelUiModel,
        val eventStatus: EventStatusUiModel?,
        val eventTitle: String,
        val eventTitleTagSpan: ParsedUniquename?,
        val address: String,
        val hostAvatar: String,
        val participants: EventParticipantsUiModel,
        val eventObject: EventObjectUiModel,
        val eventsListType: EventsListType?
    ) : EventsListItem {
        override fun isTheSame(other: EventsListItem): Boolean = other is EventItemUiModel && other.eventId == eventId
        override fun isContentTheSame(other: EventsListItem): Boolean = other == this
    }

    data class StubItemUiModel(val isInitial: Boolean, val position: Int) : EventsListItem {
        override fun isTheSame(other: EventsListItem): Boolean = other == this && isInitial
        override fun isContentTheSame(other: EventsListItem): Boolean = other == this
    }

    data class EmptyItemUiModel(
        val eventsListType: EventsListType,
        val participationCategory: EventParticipationCategory?,
        val uiModel: EventsListEmptyUiModel
    ) : EventsListItem {
        override fun isTheSame(other: EventsListItem): Boolean =
            other is EmptyItemUiModel
                && other.eventsListType == this.eventsListType
                && other.participationCategory == this.participationCategory
        override fun isContentTheSame(other: EventsListItem): Boolean = other == this
    }
}
