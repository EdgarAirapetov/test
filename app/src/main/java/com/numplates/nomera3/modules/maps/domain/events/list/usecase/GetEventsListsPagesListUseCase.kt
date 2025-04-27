package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListPageDescription
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListPageFiltersConfigUiModel
import javax.inject.Inject

class GetEventsListsPagesListUseCase @Inject constructor(private val context: Context) {
    fun invoke() = listOf(
        EventsListPageDescription(
            type = EventsListType.NEARBY,
            title = context.getString(R.string.map_events_list_page_nearby_title),
            filtersConfig = EventsListPageFiltersConfigUiModel(
                hasFilterParticipation = false,
                hasFilterEventDate = true
            )
        ),
        EventsListPageDescription(
            type = EventsListType.MY,
            title = context.getString(R.string.map_events_list_page_my_title),
            filtersConfig = EventsListPageFiltersConfigUiModel(
                hasFilterParticipation = true,
                hasFilterEventDate = true
            )
        ),
        EventsListPageDescription(
            type = EventsListType.ARCHIVE,
            title = context.getString(R.string.map_events_list_page_archive_title),
            filtersConfig = EventsListPageFiltersConfigUiModel(
                hasFilterParticipation = false,
                hasFilterEventDate = false
            )
        )
    )
}
