package com.numplates.nomera3.modules.share.data.repository

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.share.data.api.ShareApi
import com.numplates.nomera3.modules.share.data.entity.LinkResponse
import timber.log.Timber
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    private val shareApi: ShareApi
): ShareRepository {

    override suspend fun shareUserProfile(
        userId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = shareApi.shareUserProfile(userId, comment, userIds, roomIds)
            if (response.data != null || response.err != null) {
                success(response)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun getCommunityLink(
        groupId: Int,
        success: (LinkResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = shareApi.getCommunityLink(groupId)
            if (response.data != null) {
                success(response.data)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun shareCommunity(
        groupId: Int,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = shareApi.shareCommunity(
                groupId,
                comment,
                userIds,
                roomIds
            )
            if (response.data != null || response.err != null) {
                success(response)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun getPostLink(
        postId: Long,
        success: (LinkResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = shareApi.getPostLink(postId)
            if (response.data != null) {
                success(response.data)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun getPostLink(postId: Long): LinkResponse {
        val response = shareApi.getPostLink(postId)
        if (response.data != null) return response.data
        else error("Empty response")
    }
}
