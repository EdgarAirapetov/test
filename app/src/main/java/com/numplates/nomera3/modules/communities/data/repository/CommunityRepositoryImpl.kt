package com.numplates.nomera3.modules.communities.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.communities.data.api.CommunitiesApi
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.data.states.CommunityState
import com.numplates.nomera3.modules.services.data.mapper.ServiceCommunitiesModelMapper
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunitiesModel
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class CommunityRepositoryImpl @Inject constructor(
    private val api: CommunitiesApi,
    private val serviceCommunitiesModelMapper: ServiceCommunitiesModelMapper
) : CommunityRepository {

    var onSubscribeCommunitySubject = PublishSubject.create<CommunityState>()

    private val _communityListEventsFlow = MutableSharedFlow<CommunityListEvents>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun getOnSubscribeCommunityPublishSubject(): PublishSubject<CommunityState> {
        return onSubscribeCommunitySubject
    }

    override suspend fun deletionCommunityStart(communityId: Long) =
        _communityListEventsFlow.emit(CommunityListEvents.StartDeletion(communityId))

    override fun getCommunityListEvents(): Flow<CommunityListEvents> =
        _communityListEventsFlow


    override suspend fun deletionCommunityCancel(communityId: Long) =
        _communityListEventsFlow.emit(CommunityListEvents.CancelDeletion())

    override suspend fun onCreateCommunity(communityId: Long) =
        _communityListEventsFlow.emit(CommunityListEvents.CreateSuccess())

    override suspend fun getCommunities(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.getGroups(startIndex, quantity).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getServiceCommunities(startIndex: Int, quantity: Int): ServicesCommunitiesModel {
        return serviceCommunitiesModelMapper.mapCommunitiesModel(api.getGroups(startIndex, quantity).data)
    }

    override suspend fun getCommunitiesAllowedToRepost(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.getGroupsAllowedToRepost(startIndex, quantity).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }


    override suspend fun getTopCommunities(
        startIndex: Int,
        quantity: Int,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.getGroupsTop(startIndex, quantity).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun searchCommunities(
        query: String,
        startIndex: Int,
        groupType: Int,
        quantity: Int,
        isRepostAllowedOnly: Boolean,
        success: (Communities) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.searchGroups(query, startIndex, quantity, groupType, isRepostAllowedOnly).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun subscribeCommunity(groupId: Int) {
        api.subscribeGroup(groupId).data ?: throw IllegalArgumentException("Empty result")
        onSubscribeCommunitySubject.onNext(CommunityState.OnSubscribe(groupId))
    }

    override suspend fun unsubscribeCommunity(groupId: Int) {
        api.unsubscribeGroup(groupId).data ?: throw IllegalArgumentException("Empty result")
        onSubscribeCommunitySubject.onNext(CommunityState.OnUnsubscribe(groupId))
    }

    override suspend fun getCommunityUsers(
        query: String?,
        groupId: Int,
        startIndex: Int,
        quantity: Int,
        userState: Int,
        success: (CommunityMembersEntity) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.getGroupUsers(groupId, startIndex, quantity, userState, query).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getMeeraCommunityUsers(
        query: String?,
        userType: String?,
        groupId: Int,
        startIndex: Int,
        quantity: Int,
        userState: Int,
    ): ResponseWrapper<MeeraCommunityMembersEntity> {
        return api.getMeeraGroupUsers(groupId, startIndex, quantity, userState, query, userType)
    }

    override suspend fun deleteCommunity(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val result = api.removeGroup(groupId).data != null
            _communityListEventsFlow.emit(CommunityListEvents.DeleteSuccess(groupId))
            success(result)
        } catch (e: Exception) {
            _communityListEventsFlow.emit(CommunityListEvents.CancelDeletion())
            fail(e)
        }
    }

    override suspend fun blockUser(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.blockUser(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun unblockUser(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.unblockUser(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun addCommunityAdmin(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.addGroupAdmin(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun removeCommunityAdmin(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.removeGroupAdmin(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun approveMembershipRequest(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.approveUser(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun declineMembershipRequest(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.declineUser(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun removeMember(
        groupId: Int,
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.removeGroupMember(groupId, userId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun subscribeNotifications(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.subscribeGroupNotifications(groupId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun unsubscribeNotifications(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.unsubscribeGroupNotifications(groupId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun unblockAllUsers(
        groupId: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.unblockAllUsers(groupId).data != null
            success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }
}
