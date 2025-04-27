package com.numplates.nomera3.modules.communities.data.repository

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.data.states.CommunityState
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunitiesModel
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {

    suspend fun getCommunities(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getServiceCommunities(startIndex: Int, quantity: Int): ServicesCommunitiesModel

    suspend fun getCommunitiesAllowedToRepost(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getTopCommunities(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun searchCommunities(
        query: String,
        startIndex: Int,
        groupType: Int,
        quantity: Int,
        isRepostAllowedOnly: Boolean,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun subscribeCommunity(groupId: Int)

    suspend fun unsubscribeCommunity(groupId: Int)

    suspend fun getCommunityUsers(
        query: String?,
        groupId: Int,
        startIndex: Int,
        quantity: Int,
        userState: Int,
        success: (CommunityMembersEntity) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getMeeraCommunityUsers(
        query: String?,
        userType: String?,
        groupId: Int,
        startIndex: Int,
        quantity: Int,
        userState: Int,
    ): ResponseWrapper<MeeraCommunityMembersEntity>

    suspend fun deleteCommunity(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )


    suspend fun blockUser(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun unblockUser(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun addCommunityAdmin(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun removeCommunityAdmin(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun approveMembershipRequest(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun declineMembershipRequest(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )


    suspend fun removeMember(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun subscribeNotifications(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun unsubscribeNotifications(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun unblockAllUsers(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    fun getOnSubscribeCommunityPublishSubject(): PublishSubject<CommunityState>

    suspend fun deletionCommunityStart(communityId: Long)

    fun getCommunityListEvents(): Flow<CommunityListEvents>

    suspend fun deletionCommunityCancel(communityId: Long)

    suspend fun onCreateCommunity(communityId: Long)
}
