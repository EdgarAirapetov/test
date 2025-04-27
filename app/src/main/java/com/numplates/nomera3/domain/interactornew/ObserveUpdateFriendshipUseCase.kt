package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.domain.model.UpdateFriendshipModel
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUpdateFriendshipUseCase @Inject constructor(
    private val repository: ShakeRepository
) {
    fun invoke(): Flow<UpdateFriendshipModel> = repository.observeUpdateFriendship()
}
