package com.numplates.nomera3.modules.communities.domain.usecase.approvedecline

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.domain.usecase.BaseUseCaseCoroutine
import javax.inject.Inject

class DeclineMembershipRequestUseCase @Inject constructor(
    private val repository: CommunityRepository
): BaseUseCaseCoroutine<MembershipRequestUseCaseParams,Boolean> {

    override suspend fun execute(
        params: MembershipRequestUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.declineMembershipRequest(params.groupId, params.userId, success, fail)
    }
}