package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class GetCommunitiesAllowedToRepostUseCase @Inject constructor (
    private val repository: CommunityRepository
) :BaseUseCaseCoroutine<GetCommunitiesAllowedToRepostParams, Communities> {

    override suspend fun execute(
        params: GetCommunitiesAllowedToRepostParams,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.getCommunitiesAllowedToRepost(
            startIndex = params.startIndex,
            quantity = params.quantity,
            success = success,
            fail = fail
        )
    }

}


data class GetCommunitiesAllowedToRepostParams(
    val startIndex: Int,
    val quantity: Int = 20
) : DefParams()
