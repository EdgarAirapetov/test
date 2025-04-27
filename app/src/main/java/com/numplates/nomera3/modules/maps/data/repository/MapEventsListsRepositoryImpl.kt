package com.numplates.nomera3.modules.maps.data.repository

import com.google.android.gms.maps.model.LatLng
import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.data.mapper.MapEventsDataMapper
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListArchiveParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListMyParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListNearbyParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@AppScope
class MapEventsListsRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain,
    private val appSettings: AppSettings,
    private val dataMapper: MapEventsDataMapper
) : MapEventsListsRepository {

    private val eventsListsFiltersMapFlow = MutableStateFlow<Map<EventsListType, EventsListFiltersModel>>(emptyMap())

    private var latLng: LatLng? = null

    override fun setCoordinates(latLng: LatLng) {
        this.latLng = latLng
    }

    override fun setEventsListFilters(filters: EventsListFiltersModel) =
        eventsListsFiltersMapFlow.update {
            it.plus(filters.eventsListType to filters)
        }

    override fun observeEventsListsFilters(): Flow<Map<EventsListType, EventsListFiltersModel>> = eventsListsFiltersMapFlow

    override suspend fun getEventsListNearby(params: GetEventsListNearbyParamsModel): List<PostEntityResponse> =
        apiMain.getEventsListNearby(
            userId = appSettings.readUID(),
            typesString = dataMapper.mapTypeString(params.eventTypes),
            timeFilter = params.timeFilter.value,
            offset = params.offset,
            limit = params.limit,
            lat = latLng?.latitude!!,
            lon = latLng?.longitude!!,
        ).data

    override suspend fun getEventsListMy(params: GetEventsListMyParamsModel): List<PostEntityResponse> =
        apiMain.getEventsListMy(
            userId = appSettings.readUID(),
            typesString = dataMapper.mapTypeString(params.eventTypes),
            timeFilter = params.timeFilter.value,
            categoryId = params.category.value,
            offset = params.offset,
            limit = params.limit
        ).data

    override suspend fun getEventsListArchive(params: GetEventsListArchiveParamsModel): List<PostEntityResponse> =
        apiMain.getEventsListArchive(
            userId = appSettings.readUID(),
            typesString = dataMapper.mapTypeString(params.eventTypes),
            categoryId = params.category.value,
            offset = params.offset,
            limit = params.limit
        ).data
}
