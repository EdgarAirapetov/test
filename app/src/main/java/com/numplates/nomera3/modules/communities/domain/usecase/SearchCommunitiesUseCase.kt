package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class SearchGroupsUseCase @Inject constructor(
    private val repository: CommunityRepository
) : BaseUseCaseCoroutine<SearchGroupsUseCaseParams, Communities> {

    override suspend fun execute(
        params: SearchGroupsUseCaseParams,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.searchCommunities(
            params.query,
            params.startIndex,
            params.groupType,
            params.quantity,
            params.isRepostAllowedOnly,
            success,
            fail
        )
    }
}

data class SearchGroupsUseCaseParams(
    val query: String,
    val startIndex: Int,
    val groupType: Int = 0,
    val quantity: Int = 20,
    val isRepostAllowedOnly: Boolean = false
) : DefParams()