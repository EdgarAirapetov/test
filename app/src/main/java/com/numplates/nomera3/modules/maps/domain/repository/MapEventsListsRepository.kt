package com.numplates.nomera3.modules.maps.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListArchiveParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListMyParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListNearbyParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import kotlinx.coroutines.flow.Flow

interface MapEventsListsRepository {
    fun setEventsListFilters(filters: EventsListFiltersModel)
    fun observeEventsListsFilters(): Flow<Map<EventsListType, EventsListFiltersModel>>
    suspend fun getEventsListNearby(params: GetEventsListNearbyParamsModel): List<PostEntityResponse>
    suspend fun getEventsListMy(params: GetEventsListMyParamsModel): List<PostEntityResponse>
    suspend fun getEventsListArchive(params: GetEventsListArchiveParamsModel): List<PostEntityResponse>
    fun setCoordinates(latLng: LatLng)
}
