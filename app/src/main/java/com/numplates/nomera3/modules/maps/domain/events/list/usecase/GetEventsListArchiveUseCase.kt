package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListArchiveParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import javax.inject.Inject

class GetEventsListArchiveUseCase @Inject constructor(
    private val repository: MapEventsListsRepository
) {
    suspend operator fun invoke(params: GetEventsListArchiveParamsModel): List<PostEntityResponse> =
        repository.getEventsListArchive(params)
}
