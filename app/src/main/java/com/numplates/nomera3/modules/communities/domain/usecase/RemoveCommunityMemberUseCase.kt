package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class RemoveCommunityMemberUseCase @Inject constructor(
    private val repository: CommunityRepository
): BaseUseCaseCoroutine<RemoveCommunityMemberUseCaseParams, Boolean> {

    override suspend fun execute(
        params: RemoveCommunityMemberUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.removeMember(params.groupId, params.userId, success, fail)
    }
}