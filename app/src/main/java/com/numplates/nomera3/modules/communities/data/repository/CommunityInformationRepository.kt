package com.numplates.nomera3.modules.communities.data.repository

import com.numplates.nomera3.modules.communities.data.entity.Community


interface CommunityInformationRepository {

    suspend fun getCommunityInformationById(
        communityId: Int,
        success: (Community?) -> Unit,
        fail: (Exception) -> Unit
    )
}