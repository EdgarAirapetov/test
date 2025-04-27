package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class UnblockCommunityMemberUseCase @Inject constructor(
    private val repository: CommunityRepository
): BaseUseCaseCoroutine<BlockCommunityMemberUseCaseParams, Boolean> {

    override suspend fun execute(
        params: BlockCommunityMemberUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.unblockUser(params.groupId, params.userId, success, fail)
    }
}