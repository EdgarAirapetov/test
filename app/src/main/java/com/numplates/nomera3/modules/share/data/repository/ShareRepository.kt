package com.numplates.nomera3.modules.share.data.repository

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.share.data.entity.LinkResponse

interface ShareRepository {

    /**
     * Поделиться профилем пользователя в чате
     * @param - userId id пользователя, профилем которого надо делиться
     * @param - deeplinkUrl ссылка на профиль пользователя
     * @param - userIds список Id пользователей в чатах
     * @comment - присоединённый к ссылке комментарий
     */
    suspend fun shareUserProfile(
        userId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Exception) -> Unit
    )

    /**
     * Получить уникальную ссылку на сообщество
     * для того, чтобы поделиться им
     */
    suspend fun getCommunityLink(
        groupId: Int,
        success: (LinkResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    /**
     * Поделиться сообществом
     * @param - groupId id сообщества, которым надо делиться
     * @param - userIds список Id пользователей в чатах
     * @comment - присоединённый к ссылке комментарий
     */
    suspend fun shareCommunity(
        groupId: Int,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String,
        success: (ResponseWrapper<Any>) -> Unit,
        fail: (Exception) -> Unit
    )

    /**
     * Получить уникальную ссылку на пост
     * для того, чтобы поделиться им
     */
    suspend fun getPostLink(
        postId: Long,
        success: (LinkResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getPostLink(postId: Long): LinkResponse
}
