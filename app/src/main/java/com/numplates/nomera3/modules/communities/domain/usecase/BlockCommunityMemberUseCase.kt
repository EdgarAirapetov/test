package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class BlockCommunityMemberUseCase @Inject constructor(
    private val repository: CommunityRepository
): BaseUseCaseCoroutine<BlockCommunityMemberUseCaseParams, Boolean> {

    override suspend fun execute(
        params: BlockCommunityMemberUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.blockUser(params.groupId, params.userId, success, fail)
    }
}