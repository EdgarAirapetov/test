package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class UnblockAllUsersUseCase @Inject constructor(
    private val repository: CommunityRepository
) : BaseUseCaseCoroutine<CommunitiesUseCaseParams, Boolean> {

    override suspend fun execute(
        params: CommunitiesUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.unblockAllUsers(params.groupId, success, fail)
    }
}
