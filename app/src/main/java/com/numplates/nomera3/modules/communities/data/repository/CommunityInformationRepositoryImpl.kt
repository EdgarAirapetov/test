package com.numplates.nomera3.modules.communities.data.repository

import com.numplates.nomera3.modules.communities.data.api.CommunitiesApi
import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.states.CommunityNotFoundException
import javax.inject.Inject

class CommunityInformationRepositoryImpl @Inject constructor(
        private val api: CommunitiesApi
) : CommunityInformationRepository {

    override suspend fun getCommunityInformationById(
            communityId: Int,
            success: (Community?) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            val communityInformation = api.getCommunityInfo(communityId).data
            if (communityInformation != null) {
                communityInformation.also { success(it) }
            } else {
                fail(CommunityNotFoundException())
            }
        } catch (e: Exception) {
            fail(e)
        }
    }
}