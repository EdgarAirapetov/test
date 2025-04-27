package com.numplates.nomera3.modules.communities.data.api

import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface CommunitiesApi {
    @GET("/groups/get_groups")
    suspend fun getGroups(
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int
    ): ResponseWrapper<Communities>

    @GET("/groups/repost_allowed_list")
    suspend fun getGroupsAllowedToRepost(
        @Query("offset") startIndex: Int,
        @Query("limit") quantity: Int
    ): ResponseWrapper<Communities>

    @GET("/groups/get_user_groups")
    suspend fun getUserGroups(
        @Query("user_id") userId: Long,
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int
    ): ResponseWrapper<Communities>

    @GET("/groups/get_groups_top")
    suspend fun getGroupsTop(
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int
    ): ResponseWrapper<Communities>

    @GET("/groups/search_groups")
    suspend fun searchGroups(
        @Query("search") query: String?,
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int,
        @Query("group_type") groupType: Int,
        @Query("repost_allowed_only") repostAllowedOnly: Boolean
    ): ResponseWrapper<Communities>

    @Multipart
    @POST("/groups/add_group")
    suspend fun createGroup(
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("private") privateGroup: Int,
        @Query("royalty") onlyAuthor: Int,
        @Part image: Part?
    ): ResponseWrapper<EmptyModel>

    //@Multipart
    @POST("/groups/add_group")
    suspend fun createGroup(
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("private") privateGroup: Int,
        @Query("royalty") onlyAuthor: Int
    ): ResponseWrapper<EmptyModel>

    @Multipart
    @POST("/groups/set_group_info")
    suspend fun updateGroupInfo(
        @Query("group_id") groupId: Int,
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("private") privateGroup: Int,
        @Query("royalty") onlyAuthor: Int,
        @Part image: Part?
    ): ResponseWrapper<List<EmptyModel>>

    @POST("/groups/set_group_info")
    suspend fun updateGroupInfoNoImage(
        @Query("group_id") groupId: Int,
        @Query("name") name: String?,
        @Query("description") description: String?,
        @Query("private") privateGroup: Int,
        @Query("royalty") onlyAuthor: Int
    ): ResponseWrapper<List<EmptyModel>>

    @GET("/groups/subscribe_group")
    suspend fun subscribeGroup(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<List<EmptyModel>>

    @GET("/groups/unsubscribe_group")
    suspend fun unsubscribeGroup(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<List<EmptyModel>>

    @GET("/groups/get_group_info")
    suspend fun getCommunityInfo(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<Community>

    @GET("/groups/remove_group")
    suspend fun removeGroup(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<List<EmptyModel>>

    @GET("/groups/get_group_users")
    suspend fun getGroupUsers(
        @Query("group_id") groupId: Int,
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int,
        @Query("user_state") userState: Int,
        @Query("query") query: String?
    ): ResponseWrapper<CommunityMembersEntity> //0 - ALL  1 - BLOCKED 2 - NOT APPROVED YET 3 - APPROVED

    @GET("/groups/get_group_users")
    suspend fun getMeeraGroupUsers(
        @Query("group_id") groupId: Int,
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int,
        @Query("user_state") userState: Int,
        @Query("query") query: String?,
        @Query("user_type") userType: String?
    ): ResponseWrapper<MeeraCommunityMembersEntity> //0 - ALL  1 - BLOCKED 2 - NOT APPROVED YET 3 - APPROVED

    @GET("/groups/approve_user")
    suspend fun approveUser(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/decline_user")
    suspend fun declineUser(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/block_user")
    suspend fun blockUser(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/unblock_user")
    suspend fun unblockUser(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/add_group_admin")
    suspend fun addGroupAdmin(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/remove_group_admin")
    suspend fun removeGroupAdmin(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/remove_group_member")
    suspend fun removeGroupMember(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Long
    ): ResponseWrapper<Any>

    @GET("/groups/subscribe_notifications")
    suspend fun subscribeGroupNotifications(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<Any>

    @GET("/groups/unsubscribe_notifications")
    suspend fun unsubscribeGroupNotifications(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<Any>

    @GET("/groups/unblock_all_users")
    suspend fun unblockAllUsers(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<Any>
}
