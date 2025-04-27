package com.numplates.nomera3.modules.peoples.domain.repository

import com.numplates.nomera3.modules.peoples.data.entity.PeoplesRepositoryEvent
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PeopleRepository {

    suspend fun getApprovedUsers(
        limit: Int,
        offset: Int
    ): List<PeopleApprovedUserModel>

    suspend fun getRelatedUsers(
        limit: Int,
        offset: Int,
        selectedUserId: Long? = null
    ): List<PeopleRelatedUserModel>

    suspend fun getTopUsersAndCache(
        limit: Int,
        offset: Int
    )

    suspend fun getRelatedUsersAndCache(
        limit: Int,
        offset: Int
    )

    suspend fun clearSavedContent()

    fun observePeopleContentDatabase(): Flow<PeopleModel>

    suspend fun getAllContentDatabase(): PeopleModel

    suspend fun selectCommunityTab()

    suspend fun selectPeopleTab()

    suspend fun updateUserSubscribedDatabase(userId: Long, isUserSubscribed: Boolean)

    suspend fun updateUserFriendRequestDatabase(userId: Long, isAddToFriendRequest: Boolean)

    suspend fun removeRelatedUserByIdDb(userId: Long)

    fun getPeopleTabState() : SharedFlow<PeoplesRepositoryEvent>

    fun setSelectCommunityTooltipShown()

    fun getSelectCommunityTooltipShown(): Int

    fun setPeopleOnboardingShown(isOnboardingShown: Boolean)

    fun isPeopleOnboardingShown(): Boolean

    fun needShowPeopleSyncContactsDialog(): Boolean

    fun setNeedShowPeopleSyncContactsDialog(needShow: Boolean)

    fun needShowPeopleBadge(): Boolean

    fun setPeopleBadgeShown()

    fun observeNeedShowPeopleBadge(): Flow<Boolean>
}
