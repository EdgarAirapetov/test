package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.model.GetEventParticipantsParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import javax.inject.Inject

class GetEventParticipantsUseCase @Inject constructor(
    private val eventsRepository: MapEventsRepository
) {
    suspend fun invoke(params: GetEventParticipantsParamsModel): List<UserSimpleModel> =
        eventsRepository.getEventParticipants(params)
}
