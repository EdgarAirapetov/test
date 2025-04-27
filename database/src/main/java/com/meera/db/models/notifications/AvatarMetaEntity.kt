package com.meera.db.models.notifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val AVATAR_META_TABLE_NAME = "AvatarMetaEntity"

const val AVATAR_META_BIG = "big"
const val AVATAR_META_SMALL = "small"

@Entity(tableName = AVATAR_META_TABLE_NAME)
data class AvatarMetaEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 1,

    @ColumnInfo(name = AVATAR_META_BIG)
    var big: String = "",

    @ColumnInfo(name = AVATAR_META_SMALL)
    var small: String = ""
)
