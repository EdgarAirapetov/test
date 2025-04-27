package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListNearbyParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import javax.inject.Inject

class GetEventsListNearbyUseCase @Inject constructor(
    private val repository: MapEventsListsRepository
) {
    suspend operator fun invoke(params: GetEventsListNearbyParamsModel): List<PostEntityResponse> =
        repository.getEventsListNearby(params)
}
