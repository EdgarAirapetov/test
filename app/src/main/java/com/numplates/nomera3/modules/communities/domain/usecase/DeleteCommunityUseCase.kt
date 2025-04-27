package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class DeleteCommunityUseCase @Inject constructor(
    private val repo: CommunityRepository
) : BaseUseCaseCoroutine<DeleteCommunityUseCaseParams, Boolean> {

    override suspend fun execute(
        params: DeleteCommunityUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) = repo.deleteCommunity(params.communityId, success, fail)


    suspend fun deletionCommunityStart(communityId: Long) =
        repo.deletionCommunityStart(communityId)


    suspend fun deletionCommunityCancel(communityId: Long) =
        repo.deletionCommunityCancel(communityId)

}

