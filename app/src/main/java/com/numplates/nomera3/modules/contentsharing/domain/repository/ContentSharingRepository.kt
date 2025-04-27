package com.numplates.nomera3.modules.contentsharing.domain.repository

import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem

interface ContentSharingRepository {

    suspend fun getGroupsAllowedToRepost(offset: Int, limit: Int): List<CommunityEntity>

    suspend fun getShareItems(query: String?, lastContactId: String?, selectedUserId: Long?): List<ResponseShareItem>
}
