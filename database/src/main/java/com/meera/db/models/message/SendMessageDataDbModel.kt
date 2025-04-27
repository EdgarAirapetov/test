package com.meera.db.models.message

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "send_message_entity")
data class SendMessageDataDbModel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val key: Long? = null,

    @ColumnInfo(name = "data_as_json")
    val dataAsJson: String
)
