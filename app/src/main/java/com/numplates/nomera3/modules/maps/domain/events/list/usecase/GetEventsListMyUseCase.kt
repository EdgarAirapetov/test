package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListMyParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import javax.inject.Inject

class GetEventsListMyUseCase @Inject constructor(
    private val repository: MapEventsListsRepository
) {
    suspend operator fun invoke(params: GetEventsListMyParamsModel): List<PostEntityResponse> =
        repository.getEventsListMy(params)
}
