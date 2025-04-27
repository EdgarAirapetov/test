package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class MeeraGetCommunityUsersUseCase @Inject constructor(
    private val repository: CommunityRepository
) {

    suspend fun invoke(
        params: MeeraGetCommunityUsersUseCaseParams,
    ): ResponseWrapper<MeeraCommunityMembersEntity> {
        return repository.getMeeraCommunityUsers(
            query = params.query,
            userType = params.userType,
            groupId = params.groupId,
            startIndex = params.startIndex,
            quantity = params.quantity,
            userState = params.userState,
        )
    }
}

data class MeeraGetCommunityUsersUseCaseParams(
    val query: String? = null,
    val userType: String? = null,
    val groupId: Int,
    val startIndex: Int,
    val quantity: Int,
    val userState: Int = CommunityMemberState.APPROVED
) : DefParams()
