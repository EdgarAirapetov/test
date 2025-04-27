package com.numplates.nomera3.modules.reaction.domain.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReactionPostUpdatesUseCase @Inject constructor(private val reactionRepository: ReactionRepository) {
    fun invoke(): Flow<PostEntityResponse> = reactionRepository.observeReactionPostUpdates()
}
