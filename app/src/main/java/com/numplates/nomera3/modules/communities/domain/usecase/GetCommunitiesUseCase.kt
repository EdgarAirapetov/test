package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class GetCommunitiesUseCase @Inject constructor(
    private val repository: CommunityRepository
) : BaseUseCaseCoroutine<GetCommunitiesUseCaseParams, Communities> {

    override suspend fun execute(
        params: GetCommunitiesUseCaseParams,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.getCommunities(params.from, params.to, success, fail)
    }
}