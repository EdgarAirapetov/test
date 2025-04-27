package com.numplates.nomera3.modules.notifications.data.repository

import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.notifications.domain.usecase.NotificationBodyItem
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.phoenixframework.Message

interface NotificationRepository {

    fun getAmountOfNotSeenNotification(): Single<Int>

    fun setNotificationAsRead(id: String, isGroup: Boolean): Single<Boolean>

    fun setNotificationAsReadByGroupInDb(groupId: String, id: String, isGroup: Boolean): Single<Int>

    fun setNotificationAsReadByUserId(userId: Long, type: String): Single<Boolean>

    fun setAllNotificationsAsRead(): Single<ResponseWrapper<Boolean>>

    suspend fun setAllNotificationsAsReadSuspend(): Int

    fun deleteAllNotification(): Single<ResponseWrapper<Boolean>>

    fun deleteNotification(id: String): Single<ResponseWrapper<Boolean>>

    fun deleteNotification(id: String, isGroup: Boolean): Single<ResponseWrapper<Boolean>>

    suspend fun deleteNotificationSuspend(notificationId: String, isGroup: Boolean): Boolean

    suspend fun deleteOlderOrEqual(date: Long): Int

    fun setGroupOfNotificationAsSeen(ids: List<NotificationBodyItem>): Single<ResponseWrapper<Boolean>>

    fun insertNotifications(list: List<NotificationEntity>): Single<List<Long>>

    suspend fun insertNotificationsSuspend(list: List<NotificationEntity>): List<Long>

    fun markAsReadPostNotification(id: Long): Observable<Message>

    fun markAsReadCommentNotification(id: Long): Observable<Message>

    fun getUnreadNotificationCount(): Single<Int>

    suspend fun clearAllCachedNotifications()

    suspend fun clearNotificationsByGroupDb(notificationGroupId: String)

    suspend fun markNotificationAsRead(
        notificationId: String,
        isGroup: Boolean,
        unreadNotifications: List<NotificationUiModel>
    )

    suspend fun triggerNotificationCacheUpdate(notificationId: String)

    suspend fun deleteBlockUserNotifications()

    suspend fun fetchNotificationsRemote(milliSecondOffset: Long, limit: Int): List<NotificationEntity>

    suspend fun fetchAndCacheNotifications(milliSecondOffset: Long, limit: Int): List<NotificationEntity>

    fun subscribeToNotifications(): Flow<List<NotificationEntity>>

    suspend fun updateNotificationsAvatarMomentsState(
        momentsState: UserMomentsStateUpdateModel
    ): List<NotificationEntity>

    suspend fun convertHeaderAction()

    fun getCountAllNotifications(): Flow<Int>
}
