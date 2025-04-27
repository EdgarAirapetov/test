package com.numplates.nomera3.modules.services.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunitiesModel
import javax.inject.Inject

class GetCommunitiesSuspendUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {

    suspend fun invoke(pageLimit: Int, offset: Int): ServicesCommunitiesModel {
        return communityRepository.getServiceCommunities(offset, pageLimit)
    }

}
