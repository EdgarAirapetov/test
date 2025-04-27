package com.meera.db.models.notifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

const val NOTIFICATION_ENTITY_TABLE_NAME = "NotificationEntity"
const val NOTIFICATION_ENTITY_ID = "id"
const val NOTIFICATION_ENTITY_READ = "read"
const val NOTIFICATION_ENTITY_GROUP = "is_group"
const val NOTIFICATION_ENTITY_GROUP_ID = "group_id"
const val NOTIFICATION_ENTITY_COUNT = "count"
const val NOTIFICATION_ENTITY_DATE = "date"
const val NOTIFICATION_ENTITY_DATE_LONG = "date_long"
const val NOTIFICATION_ENTITY_USERS = "users"
const val NOTIFICATION_ENTITY_TYPE = "type"
const val NOTIFICATION_ENTITY_META = "meta"
const val NOTIFICATION_ENTITY_DATE_GROUP = "date_group"
const val NOTIFICATION_ENTITY_INFO_SECTION = "info_section"
const val NOTIFICATION_ENTITY_CHANGED_FLAG = "changed_flag"


@Entity(tableName = NOTIFICATION_ENTITY_TABLE_NAME)
data class NotificationEntity(

    @PrimaryKey
    @ColumnInfo(name = NOTIFICATION_ENTITY_ID)
    var id: String = "",

    @ColumnInfo(name = NOTIFICATION_ENTITY_READ)
    var isRead: Boolean = false,

    @ColumnInfo(name = NOTIFICATION_ENTITY_GROUP)
    var isGroup: Boolean = false,

    @ColumnInfo(name = NOTIFICATION_ENTITY_GROUP_ID)
    var groupId: String? = null,

    @ColumnInfo(name = NOTIFICATION_ENTITY_DATE)
    var date: Date = Date(),

    @ColumnInfo(name = NOTIFICATION_ENTITY_COUNT)
    var count: Int = 1,

    @ColumnInfo(name = NOTIFICATION_ENTITY_USERS)
    var users: List<UserEntity> = listOf(),

    @ColumnInfo(name = NOTIFICATION_ENTITY_TYPE)
    var type: String = "",

    @ColumnInfo(name = NOTIFICATION_ENTITY_META)
    var meta: MetaNotificationEntity? = null,

    @ColumnInfo(name = NOTIFICATION_ENTITY_INFO_SECTION)
    var infoSection: InfoSectionEntity? = null,

    @ColumnInfo(name = NOTIFICATION_ENTITY_DATE_GROUP)
    var dateGroup: String = "",

    @ColumnInfo(name = NOTIFICATION_ENTITY_CHANGED_FLAG)
    var changedFlag: Int = -500,

    @ColumnInfo(name = NOTIFICATION_ENTITY_DATE_LONG)
    var dateLong: Long = 0
)


