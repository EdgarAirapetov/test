package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.model.GetMapFriendsParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapFriendsRepository
import javax.inject.Inject

class GetMapFriendsUseCase @Inject constructor(
    private val repository: MapFriendsRepository
) {
    suspend operator fun invoke(params: GetMapFriendsParamsModel) = repository.getFriends(params)
}
