package com.meera.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_CHANGED_FLAG
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_DATE
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_DATE_LONG
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_GROUP_ID
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_ID
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_INFO_SECTION
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_READ
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_TABLE_NAME
import com.meera.db.models.notifications.NOTIFICATION_ENTITY_TYPE
import com.meera.db.models.notifications.NotificationEntity
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(value: List<NotificationEntity>): Single<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuspend(value: List<NotificationEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(value: NotificationEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_ID = :id")
    suspend fun getById(id: String): NotificationEntity?

    @Query("UPDATE $NOTIFICATION_ENTITY_TABLE_NAME SET $NOTIFICATION_ENTITY_READ = 1")
    suspend fun readAll(): Int

    @Query("UPDATE $NOTIFICATION_ENTITY_TABLE_NAME SET $NOTIFICATION_ENTITY_READ = :isRead WHERE $NOTIFICATION_ENTITY_ID = :notificationId")
    fun updateIsReadById(isRead: Boolean, notificationId: String): Int

    @Query("UPDATE $NOTIFICATION_ENTITY_TABLE_NAME SET $NOTIFICATION_ENTITY_READ = :isRead WHERE $NOTIFICATION_ENTITY_GROUP_ID = :groupId")
    fun updateIsReadByGroupId(isRead: Boolean, groupId: String): Int

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID IS NULL")
    fun getAllNotificationPaged(): DataSource.Factory<Int, NotificationEntity>

    @Query("SELECT count(*) FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID IS NULL")
    fun getCountAllNotifications(): Flow<Int>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID IS NULL ORDER BY $NOTIFICATION_ENTITY_DATE DESC")
    fun flowAllNotificationsDesc(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID IS NULL")
    fun getAllNotifications(): List<NotificationEntity>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME")
    fun getAllExistingNotifications(): List<NotificationEntity>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID IS NULL AND $NOTIFICATION_ENTITY_DATE_LONG >= :timestamp")
    fun getNotificationsAfterTime(timestamp: Long): List<NotificationEntity>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID = :groupId")
    fun getAllNotificationPagedByGroupId(groupId: String): DataSource.Factory<Int, NotificationEntity>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID = :groupId ORDER BY $NOTIFICATION_ENTITY_DATE ASC")
    suspend fun allByGroupId(groupId: String): List<NotificationEntity>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_TYPE = :type ORDER BY $NOTIFICATION_ENTITY_DATE ASC")
    fun allByType(type: String): Flowable<List<NotificationEntity>>

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_ID = :id")
    fun getEntityById(id: String): NotificationEntity

    @Query("SELECT * FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_INFO_SECTION IS NOT NULL")
    suspend fun getAllInfoSections(): List<NotificationEntity>

    @Query("UPDATE $NOTIFICATION_ENTITY_TABLE_NAME SET $NOTIFICATION_ENTITY_CHANGED_FLAG = $NOTIFICATION_ENTITY_CHANGED_FLAG + 1 WHERE $NOTIFICATION_ENTITY_ID = :id")
    suspend fun triggerItemUpdate(id: String)

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_GROUP_ID = :groupId")
    suspend fun deleteAllByGroupIdSuspend(groupId: String)

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_ID = :id")
    fun deleteById(id: String): Int

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_ID IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_ID = :id")
    suspend fun deleteByIdSuspend(id: String)

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME WHERE $NOTIFICATION_ENTITY_DATE_LONG <= :longDate")
    suspend fun deleteOlderOrEqual(longDate: Long): Int

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME")
    fun deleteAllNotifications(): Single<Int>

    @Query("DELETE FROM $NOTIFICATION_ENTITY_TABLE_NAME")
    suspend fun deleteAllNotificationsSuspend()
}
