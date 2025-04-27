package com.numplates.nomera3.modules.contentsharing.domain.usecase

import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.contentsharing.domain.repository.ContentSharingRepository
import javax.inject.Inject

class GroupsAllowedToRepostUseCase @Inject constructor(
    private val repository: ContentSharingRepository
) {

    suspend fun invoke(offset: Int, limit: Int): List<CommunityEntity> {
        return repository.getGroupsAllowedToRepost(offset, limit)
    }

}
