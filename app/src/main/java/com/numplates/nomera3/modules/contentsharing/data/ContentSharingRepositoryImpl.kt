package com.numplates.nomera3.modules.contentsharing.data

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.contentsharing.domain.repository.ContentSharingRepository
import com.numplates.nomera3.modules.share.data.api.ShareApi
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import javax.inject.Inject

private const val DEFAULT_LIMIT = 30

class ContentSharingRepositoryImpl @Inject constructor(
    private val mainApi: ApiMain,
    private val shareApi: ShareApi,
) : ContentSharingRepository {

    override suspend fun getGroupsAllowedToRepost(offset: Int, limit: Int): List<CommunityEntity> {
        val response = mainApi.getGroupsAllowedToRepost(offset, limit)
        if (response.data != null) {
            return response.data.communityEntities.orEmpty().filterNotNull()
        } else if (response.err != null) {
            error(response.err)
        }
        return emptyList()
    }

    override suspend fun getShareItems(
        query: String?,
        lastContactId: String?,
        selectedUserId: Long?
    ): List<ResponseShareItem> {
        val response = shareApi.getShareItems(
            limit = DEFAULT_LIMIT,
            query = query,
            lastId = lastContactId,
            selectedUserId = selectedUserId
        )
        if (response.data != null) {
            return response.data.orEmpty()
        } else if (response.err != null) {
            error(response.err)
        }
        return emptyList()
    }
}
