package com.numplates.nomera3.modules.peoples.data.repository

import android.content.SharedPreferences
import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.meera.db.dao.PeopleApprovedUsersDao
import com.meera.db.dao.PeopleRelatedUsersDao
import com.numplates.nomera3.modules.peoples.data.api.PeopleApi
import com.numplates.nomera3.modules.peoples.data.entity.PeoplesRepositoryEvent
import com.numplates.nomera3.modules.peoples.data.mapper.PeopleDataMapper
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DEFAULT_PEOPLE_OFF_SET = 0

@AppScope
class PeopleRepositoryImpl @Inject constructor(
    private val api: PeopleApi,
    private val appSettings: AppSettings,
    private val peopleRelatedUsersDao: PeopleRelatedUsersDao,
    private val peopleApprovedUsersDao: PeopleApprovedUsersDao,
    private val mapper: PeopleDataMapper
) : PeopleRepository {

    private val _peopleRepositoryEffect = MutableSharedFlow<PeoplesRepositoryEvent>()
    val peopleRepositoryEffect: SharedFlow<PeoplesRepositoryEvent> = _peopleRepositoryEffect

    override suspend fun getApprovedUsers(
        limit: Int,
        offset: Int
    ): List<PeopleApprovedUserModel> {
        val approvedUsers = api.getTopUsers(
            limit = limit,
            offset = offset
        )
        if (offset == DEFAULT_PEOPLE_OFF_SET) {
            withContext(Dispatchers.IO) {
                peopleApprovedUsersDao.clear()
                peopleApprovedUsersDao.insertApprovedUsers(mapper.mapApprovedDtoModelToDbModel(approvedUsers.data))
            }
        }
        return mapper.mapApprovedDtoModelToApprovedUsers(
            approvedUsers = approvedUsers.data
        )
    }

    override suspend fun getRelatedUsers(
        limit: Int,
        offset: Int,
        selectedUserId: Long?
    ): List<PeopleRelatedUserModel> {
        val relatedUsers = api.getRelatedUsers(
            limit = limit,
            offset = offset,
            selectedUserId = selectedUserId
        )
        if (offset == DEFAULT_PEOPLE_OFF_SET) {
            withContext(Dispatchers.IO) {
                peopleRelatedUsersDao.clear()
                peopleRelatedUsersDao.insertRelatedUsers(mapper.mapRelatedUsersDtoModelToDbModel(relatedUsers.data))
            }
        }
        return mapper.mapRelatedUsersDtoModel(relatedUsers.data)
    }

    override fun observePeopleContentDatabase(): Flow<PeopleModel> {
        val approvedUsersFlow = peopleApprovedUsersDao
            .observeApprovedUsers()
            .map(mapper::mapApprovedDbModelToApprovedUsers)
        val relatedUsersFlow = peopleRelatedUsersDao
            .observeRelatedUsers()
            .map(mapper::mapRelatedUsersDbModel)
        return combine(approvedUsersFlow, relatedUsersFlow) { approvedUsers, relatedUsers ->
            PeopleModel(
                approvedUsers = approvedUsers,
                relatedUsers = relatedUsers
            )
        }
    }

    override suspend fun clearSavedContent() {
        withContext(Dispatchers.IO) {
            peopleApprovedUsersDao.clear()
            peopleRelatedUsersDao.clear()
        }
    }

    override suspend fun selectCommunityTab() {
        _peopleRepositoryEffect.emit(PeoplesRepositoryEvent.SelectCommunityViewEvent)
    }

    override suspend fun selectPeopleTab() {
        _peopleRepositoryEffect.emit(PeoplesRepositoryEvent.SelectPeopleViewEvent)
    }

    override fun getPeopleTabState(): SharedFlow<PeoplesRepositoryEvent> {
        return peopleRepositoryEffect
    }

    override fun setSelectCommunityTooltipShown() {
        val shownTimes = appSettings.isSelectCommunityTooltipShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isSelectCommunityTooltipShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(
            AppSettings.KEY_IS_SELECT_COMMUNITY_TOOL_TIP_SHOWN_TIMES
        )
    }

    override fun getSelectCommunityTooltipShown(): Int {
        return appSettings.isSelectCommunityTooltipShownTimes
    }

    override fun setPeopleOnboardingShown(isOnboardingShown: Boolean) {
        appSettings.isPeopleOnboardingShown = isOnboardingShown
    }

    override fun isPeopleOnboardingShown(): Boolean = appSettings.isPeopleOnboardingShown

    override suspend fun getAllContentDatabase(): PeopleModel {
        val relatedUsers = peopleRelatedUsersDao.getRelatedUsers()
        val approvedUsers = peopleApprovedUsersDao.getApprovedUsers()
        return PeopleModel(
            relatedUsers = mapper.mapRelatedUsersDbModel(relatedUsers),
            approvedUsers = mapper.mapApprovedDbModelToApprovedUsers(approvedUsers)
        )
    }

    override suspend fun getTopUsersAndCache(
        limit: Int,
        offset: Int
    ) {
        runCatching {
            api.getTopUsers(
                limit = limit,
                offset = offset
            )
        }.onSuccess { topUsers ->
            if (!topUsers.data.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    peopleApprovedUsersDao.clear()
                    peopleApprovedUsersDao.insertApprovedUsers(mapper.mapApprovedDtoModelToDbModel(topUsers.data))
                }
            }
        }.onFailure { t ->
            throw t
        }
    }

    override suspend fun getRelatedUsersAndCache(
        limit: Int,
        offset: Int
    ) {
        runCatching {
            api.getRelatedUsers(
                limit = limit,
                offset = offset
            )
        }.onSuccess { related ->
            if (!related.data.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    peopleRelatedUsersDao.clear()
                    peopleRelatedUsersDao.insertRelatedUsers(mapper.mapRelatedUsersDtoModelToDbModel(related.data))
                }
            }
        }.onFailure { t ->
            throw t
        }
    }

    override suspend fun updateUserFriendRequestDatabase(
        userId: Long,
        isAddToFriendRequest: Boolean
    ) {
        peopleRelatedUsersDao.updateHasFriendRequest(
            userId = userId,
            isAddToFriendRequest = isAddToFriendRequest
        )
    }

    override suspend fun removeRelatedUserByIdDb(userId: Long) {
        peopleRelatedUsersDao.removeUserById(userId)
    }

    override suspend fun updateUserSubscribedDatabase(
        userId: Long,
        isUserSubscribed: Boolean
    ) {
        peopleApprovedUsersDao.updateUserSubscribed(
            userId = userId,
            isUserSubscribed = isUserSubscribed
        )
    }

    override fun needShowPeopleSyncContactsDialog(): Boolean {
        return appSettings.needShowPeopleSyncContactsDialog
    }

    override fun setNeedShowPeopleSyncContactsDialog(needShow: Boolean) {
        appSettings.needShowPeopleSyncContactsDialog = needShow
    }

    override fun needShowPeopleBadge(): Boolean = appSettings.needShowPeopleBadge

    override fun setPeopleBadgeShown() {
        appSettings.needShowPeopleBadge = false
    }

    override fun observeNeedShowPeopleBadge(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == AppSettings.KEY_SHOW_PEOPLE_BADGE) {
                val needShowPeopleBadge = appSettings.needShowPeopleBadge
                trySend(needShowPeopleBadge)
            }
        }
        appSettings.registerPreferencesChangeListener(listener)
        awaitClose { appSettings.unRegisterPreferencesChangeListener(listener) }
    }
}
