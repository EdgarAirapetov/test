package com.meera.db.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "app_settings")
data class AppSettingsEntity(

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "last_updated_dialogs")
    var lastUpdatedDialogs: Long = 0
)
