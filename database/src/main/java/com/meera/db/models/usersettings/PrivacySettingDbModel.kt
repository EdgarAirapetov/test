package com.meera.db.models.usersettings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_settings")
data class PrivacySettingDbModel(

    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: Int?,

    @ColumnInfo(name = "count_blacklist")
    val countBlacklist: Int? = null,

    @ColumnInfo(name = "count_whitelist")
    val countWhitelist: Int? = null
)
