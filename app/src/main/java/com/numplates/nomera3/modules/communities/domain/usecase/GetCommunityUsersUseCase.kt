package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class GetCommunityUsersUseCase @Inject constructor(
    private val repository: CommunityRepository
) : BaseUseCaseCoroutine<GetCommunityUsersUseCaseParams, CommunityMembersEntity> {

    override suspend fun execute(
        params: GetCommunityUsersUseCaseParams,
        success: (CommunityMembersEntity) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.getCommunityUsers(
            query = params.query,
            groupId = params.groupId,
            startIndex = params.startIndex,
            quantity = params.quantity,
            userState = params.userState,
            success = success,
            fail = fail
        )
    }
}

data class GetCommunityUsersUseCaseParams(
    val query: String? = null,
    val groupId: Int,
    val startIndex: Int,
    val quantity: Int,
    val userState: Int = CommunityMemberState.APPROVED
) : DefParams()
