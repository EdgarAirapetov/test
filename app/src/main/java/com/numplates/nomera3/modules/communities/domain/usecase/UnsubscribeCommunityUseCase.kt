package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class UnsubscribeCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {

    suspend fun invoke(params: CommunitiesUseCaseParams) {
        repository.unsubscribeCommunity(params.groupId)
    }

}
