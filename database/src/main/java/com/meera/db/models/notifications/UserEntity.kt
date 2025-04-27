package com.meera.db.models.notifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val USER_ENTITY_TABLE_NAME = "UserEntity"
const val USER_ENTITY_USER_ID = "user_id"
const val USER_ENTITY_ACCOUNT_TYPE = " account_type"
const val USER_ENTITY_NAME = "name"
const val USER_ENTITY_AVATAR = "avatar"
const val USER_ENTITY_ACCOUNT_COLOR = "account_color"
const val USER_ENTITY_BIRTHDAY = "birthday"
const val USER_ENTITY_HAS_MOMENTS = "has_moments"
const val USER_ENTITY_HAS_NEW_MOMENTS = "has_new_moments"

@Entity(tableName = USER_ENTITY_TABLE_NAME)
data class UserEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = USER_ENTITY_USER_ID)
    var userId: Int = 1,

    @ColumnInfo(name = USER_ENTITY_ACCOUNT_TYPE)
    var accountType: Int = 1,

    @ColumnInfo(name = USER_ENTITY_NAME)
    var name: String = "",

    @ColumnInfo(name = USER_ENTITY_AVATAR)
    var avatar: AvatarMetaEntity = AvatarMetaEntity(),

    @ColumnInfo(name = USER_ENTITY_AVATAR)
    var gender: Int? = null,

    @ColumnInfo(name = USER_ENTITY_ACCOUNT_COLOR)
    var accountColor: Int = 1,

    @ColumnInfo(name = USER_ENTITY_BIRTHDAY)
    var birthday: Long = 1,

    @ColumnInfo(name = USER_ENTITY_HAS_MOMENTS)
    val hasMoments: Boolean? = false,

    @ColumnInfo(name = USER_ENTITY_HAS_NEW_MOMENTS)
    val hasNewMoments: Boolean? = false

)
