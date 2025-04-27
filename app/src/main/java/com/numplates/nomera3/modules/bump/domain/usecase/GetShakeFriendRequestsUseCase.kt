package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShakeFriendRequestsUseCase @Inject constructor(
    private val repository: ShakeRepository
) {
    fun invoke(): Flow<List<UserShakeModel>> = repository.observeShakeFriendRequests()
}
