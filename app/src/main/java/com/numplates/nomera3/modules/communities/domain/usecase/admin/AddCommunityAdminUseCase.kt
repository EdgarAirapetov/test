package com.numplates.nomera3.modules.communities.domain.usecase.admin

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.domain.usecase.BaseUseCaseCoroutine
import javax.inject.Inject

class AddCommunityAdminUseCase @Inject constructor(
    private val repository: CommunityRepository
) : BaseUseCaseCoroutine<CommunityAdminUseCaseParams, Boolean> {
    override suspend fun execute(
        params: CommunityAdminUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.addCommunityAdmin(params.groupId, params.userId, success, fail)
    }
}