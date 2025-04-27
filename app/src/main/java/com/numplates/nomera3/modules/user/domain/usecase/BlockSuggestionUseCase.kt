package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class BlockSuggestionUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke(userId: Long) = repository.blockSuggestionById(userId)
}
