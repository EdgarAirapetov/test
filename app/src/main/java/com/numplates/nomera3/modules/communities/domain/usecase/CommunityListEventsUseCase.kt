package com.numplates.nomera3.modules.communities.domain.usecase

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import javax.inject.Inject

class CommunityListEventsUseCase @Inject constructor(
    private val repo: CommunityRepository
) {
    
    fun invoke() = repo.getCommunityListEvents()

}
