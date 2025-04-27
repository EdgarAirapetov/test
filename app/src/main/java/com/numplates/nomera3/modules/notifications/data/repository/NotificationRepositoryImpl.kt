package com.numplates.nomera3.modules.notifications.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.simpleName
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.DataStore
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.notifications.data.api.NotificationApi
import com.numplates.nomera3.modules.notifications.data.api.WebSocketApiEnum
import com.numplates.nomera3.modules.notifications.data.mapper.ListOfNotificationMapper
import com.numplates.nomera3.modules.notifications.domain.usecase.NotificationBodyItem
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.phoenixframework.Message
import timber.log.Timber
import javax.inject.Inject

private const val CHECK_USER_BLOCKED_LIMIT_NOTIFICATIONS = 100

@AppScope
class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi,
    private val ds: DataStore,
    private val webSocket: WebSocketMainChannel,
    private val listOfNotificationMapper: ListOfNotificationMapper,
) : NotificationRepository {

    override fun getAmountOfNotSeenNotification(): Single<Int> =
        api.fetchNewNotificationCount()
            .subscribeOn(Schedulers.io())
            .map { it.data.count }

    override fun setNotificationAsRead(id: String, isGroup: Boolean): Single<Boolean> =
        api.setNotificationAsRead(
            hashMapOf(
                "id" to id,
                "is_group" to isGroup
            )
        ).flatMap {
            Single.create<Boolean> {
                try {
                    val rows = ds.notificationDao().updateIsReadById(true, id)
                    Timber.d("setNotificationAsRead with rows = $rows")
                    it.onSuccess(true)
                } catch (e: Exception) {
                    Timber.e(e)
                    it.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())

    override fun getUnreadNotificationCount(): Single<Int> =
        api.getUnreadEventsCounter()
            .map { it.data.count ?: 0 }

    override fun setNotificationAsReadByGroupInDb(groupId: String, id: String, isGroup: Boolean): Single<Int> =
        api.setNotificationAsRead(
            hashMapOf(
                "id" to id,
                "is_group" to isGroup
            )
        ).flatMap {
            Single.create<Int> {
                try {
                    val rows = ds.notificationDao().updateIsReadByGroupId(true, groupId)
                    Timber.d("setNotificationAsRead with rows = $rows")
                    it.onSuccess(rows)
                } catch (e: Exception) {
                    Timber.e(e)
                    it.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())

    override fun setNotificationAsReadByUserId(userId: Long, type: String): Single<Boolean> =
        Single.create { e ->
            try {
                ds.notificationDao().allByType(type)
                    .map { notifications ->
                        notifications
                            .forEach { notification ->
                                val user = notification.users.find { it.userId == userId.toInt() }
                                if (user != null && !notification.isRead) {
                                    Timber.d("setNotificationAsReadByUserId = ${user.name}")
                                    setNotificationAsRead(notification.id, false)
                                } else Single.just(false)
                            }
                    }
                    .subscribe({
                        e.onSuccess(true)
                    }, {
                        Timber.e(it)
                        e.onError(it)
                    })
            } catch (ex: Exception) {
                Timber.e(ex)
                e.onError(ex)
            }
        }


    override fun setAllNotificationsAsRead(): Single<ResponseWrapper<Boolean>> =
        api.setAllNotificationsAsRead()
            .subscribeOn(Schedulers.io())

    override suspend fun setAllNotificationsAsReadSuspend() = withContext(Dispatchers.IO) {
        api.setAllNotificationsAsReadSuspend()
        ds.notificationDao().readAll()
    }

    override fun deleteAllNotification(): Single<ResponseWrapper<Boolean>> =
        api.deleteAllEvents()
            .subscribeOn(Schedulers.io())

    override fun deleteNotification(id: String): Single<ResponseWrapper<Boolean>> =
        api.deleteEvent(id)
            .subscribeOn(Schedulers.io())

    override fun deleteNotification(id: String, isGroup: Boolean): Single<ResponseWrapper<Boolean>> =
        api.deleteEventByGroupId(makeParam(id, isGroup))

    override suspend fun deleteNotificationSuspend(notificationId: String, isGroup: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val notification = requireNotNull(ds.notificationDao().getById(notificationId))
                val response = api.deleteEventByGroupIdSuspend(
                    notificationId = notificationId,
                    isNotificationGroup = notification.isGroup,
                )
                val isSuccess = response.data
                if (isSuccess) {
                    if (notification.isGroup) {
                        ds.notificationDao().deleteAllByGroupIdSuspend(notificationId)
                        ds.notificationDao().deleteByIdSuspend(notificationId)
                    } else if (notification.groupId.isNullOrBlank()) {
                        ds.notificationDao().deleteByIdSuspend(notificationId)
                    } else {
                        ds.notificationDao().deleteByIdSuspend(notificationId)
                        val groupId = requireNotNull(notification.groupId)
                        val groupNotification = requireNotNull(ds.notificationDao().getById(groupId))
                        val grouped = ds.notificationDao().allByGroupId(groupId)
                        if (isEmptyNestedNotification(groupNotification)) {
                            ds.notificationDao().deleteByIdSuspend(groupId)
                        } else if (isLastNestedNotification(groupNotification)) {
                            // TODO https://nomera.atlassian.net/browse/BR-27484
                            ds.notificationDao().update(grouped.first().copy(groupId = null))
                            ds.notificationDao().deleteByIdSuspend(groupId)
                        } else {
                            val updated = groupNotification.copy(
                                count = groupNotification.count - 1,
                                users = groupNotification.users.filter { user ->
                                    grouped.any { child -> child.users.any { usr -> usr.userId == user.userId } }
                                }
                            )
                            ds.notificationDao().update(updated)
                        }
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.tag(this.simpleName).e(e)
                false
            }
        }

    private fun isEmptyNestedNotification(notification: NotificationEntity): Boolean {
        return notification.count == NOTIFICATION_GROUP_EMPTY_CHILD_COUNT
    }

    private fun isLastNestedNotification(notification: NotificationEntity): Boolean {
        return notification.count < NOTIFICATION_GROUP_MINIMUM_CHILD_COUNT
    }

    override suspend fun deleteOlderOrEqual(date: Long): Int =
        ds.notificationDao().deleteOlderOrEqual(date)

    override fun setGroupOfNotificationAsSeen(ids: List<NotificationBodyItem>): Single<ResponseWrapper<Boolean>> =
        api.setGroupOfNotificationsAsSeen(makeParamsForSeen(ids))
            .subscribeOn(Schedulers.io())

    override fun insertNotifications(list: List<NotificationEntity>): Single<List<Long>> =
        ds.notificationDao().insertAll(list)

    override suspend fun insertNotificationsSuspend(list: List<NotificationEntity>): List<Long> =
        ds.notificationDao().insertAllSuspend(list)

    private fun makeParam(id: String, isGroup: Boolean): HashMap<String, Any> =
        hashMapOf(
            "id" to id,
            "is_group" to isGroup
        )

    private fun makeParamsForSeen(ids: List<NotificationBodyItem>): HashMap<String, Any> =
        hashMapOf(
            "ids" to ids
        )

    private fun makeParamsForReadPostNotification(id: Long) =
        hashMapOf(
            "post_id" to id
        )

    private fun makeParamsForReadCommentNotification(id: Long) =
        makeParamsForReadPostNotification(id)

    override fun markAsReadPostNotification(id: Long): Observable<Message> =
        webSocket.createCommonRequest(
            WebSocketApiEnum.WEB_SOCKET_MARK_POST_NOTIFICATION.request,
            makeParamsForReadPostNotification(id)
        )

    override fun markAsReadCommentNotification(id: Long): Observable<Message> =
        webSocket.createCommonRequest(
            WebSocketApiEnum.WEB_SOCKET_MARK_POST_COMMENT_NOTIFICATION.request,
            makeParamsForReadCommentNotification(id)
        )

    override suspend fun clearAllCachedNotifications() = withContext(Dispatchers.IO) {
        ds.notificationDao().deleteAllNotificationsSuspend()
    }

    override suspend fun clearNotificationsByGroupDb(notificationGroupId: String) {
        ds.notificationDao().deleteAllByGroupIdSuspend(notificationGroupId)
    }

    override suspend fun markNotificationAsRead(
        notificationId: String,
        isGroup: Boolean,
        unreadNotifications: List<NotificationUiModel>
    ) {
        val body = hashMapOf<String, Any>(
            "id" to notificationId,
            "is_group" to isGroup
        )
        val isRequestSuccess = api.markNotificationAsRead(body).data
        if (isRequestSuccess) {
            withContext(Dispatchers.IO) {
                if (isGroup) {
                    ds.notificationDao().updateIsReadByGroupId(isRead = true, groupId = notificationId)
                }
                ds.notificationDao().updateIsReadById(isRead = true, notificationId = notificationId)
                markAsReadParentNotification(unreadNotifications)
            }
        } else {
            error("Failed to mark notification as read")
        }
    }

    private fun markAsReadParentNotification(unreadNotifications: List<NotificationUiModel>) {
        val unreadCount = unreadNotifications.size
        if (unreadCount == 1) {
            ds.notificationDao().updateIsReadById(
                isRead = true,
                notificationId = unreadNotifications.first().groupId
            )
        }
    }

    override suspend fun triggerNotificationCacheUpdate(notificationId: String) =
        ds.notificationDao().triggerItemUpdate(notificationId)

    override suspend fun deleteBlockUserNotifications() = withContext(Dispatchers.IO) {
        val dbNotifications = ds.notificationDao().getAllNotifications()
        val networkNotifications = fetchNotificationsRemote(0, CHECK_USER_BLOCKED_LIMIT_NOTIFICATIONS)
        val networkNotificationsIds = networkNotifications.map { it.id }
        val common = dbNotifications.map { it.id }.intersect(networkNotificationsIds.toSet())
        if (common.isNotEmpty()) {
            val res = (dbNotifications + networkNotifications).filter { it.id !in common && it.infoSection == null }
            res.forEach { notification ->
                if (notification.isGroup) {
                    ds.notificationDao().deleteById(notification.id)
                }
            }
        }
    }

    override suspend fun fetchNotificationsRemote(milliSecondOffset: Long, limit: Int): List<NotificationEntity> {
        return api.fetchGroupedNotificationSuspend(limit, milliSecondOffset)
            .let { listOfNotificationMapper.apply(it.data) }
    }

    override suspend fun fetchAndCacheNotifications(milliSecondOffset: Long, limit: Int): List<NotificationEntity> =
        withContext(Dispatchers.IO) {
            val remoteNotifications = fetchNotificationsRemote(milliSecondOffset, limit)
            val localNotifications = ds.notificationDao().getNotificationsAfterTime(milliSecondOffset)
            val removeRequired = mutableListOf<NotificationEntity>()
            for (notification in localNotifications) {
                if (remoteNotifications.none { it.date.time == notification.date.time }) {
                    removeRequired.add(notification)
                }
            }
            Timber.d("fetchAndCacheNotifications removeRequired = $removeRequired")
            ds.notificationDao().deleteByIds(removeRequired.map { it.id })
            insertNotificationsSuspend(remoteNotifications)
            remoteNotifications
        }

    override fun subscribeToNotifications(): Flow<List<NotificationEntity>> {
        return ds.notificationDao().flowAllNotificationsDesc()
    }

    override suspend fun updateNotificationsAvatarMomentsState(
        momentsState: UserMomentsStateUpdateModel
    ) = withContext(Dispatchers.IO) {
        val allNotifications = ds.notificationDao().getAllExistingNotifications()
        val updatedNotifications = mutableListOf<NotificationEntity>()
        allNotifications.forEach { notification ->
            if (notification.users.isNotEmpty()) {
                for (i in 0 until notification.users.size) {
                    if (notification.users[i].userId.toLong() == momentsState.userId) {
                        val updatedUser = notification.users[i].copy(
                            hasMoments = momentsState.hasMoments,
                            hasNewMoments = momentsState.hasNewMoments
                        )

                        val notificationUsers = notification.users.toMutableList()
                        notificationUsers[i] = updatedUser

                        val updatedNotification = notification.copy(users = notificationUsers)
                        updatedNotifications.add(updatedNotification)
                    }
                }
            }
        }
        ds.notificationDao().update(updatedNotifications)
        return@withContext updatedNotifications
    }

    override suspend fun convertHeaderAction() {
        val readAllSection = ds.notificationDao().getAllInfoSections()
            .find { it.infoSection?.action == ACTION_TYPE_READ_ALL } ?: return
        val infoSection = readAllSection.infoSection?.copy(action = ACTION_TYPE_DELETE_ALL)
        val modified = readAllSection.copy(infoSection = infoSection)
        ds.notificationDao().update(modified)
    }

    override fun getCountAllNotifications(): Flow<Int> = ds.notificationDao().getCountAllNotifications()

    companion object {
        private const val NOTIFICATION_GROUP_MINIMUM_CHILD_COUNT = 2
        private const val NOTIFICATION_GROUP_EMPTY_CHILD_COUNT = 0
    }
}
