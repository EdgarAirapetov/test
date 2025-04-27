package com.numplates.nomera3.modules.chat.toolbar.data.entity

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "online_chat_status")
data class OnlineChatStatusEntity(

    @PrimaryKey
    @SerializedName("room_id")
    @ColumnInfo(name = "room_id")
    var roomId: Long,

    @Nullable
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: String?,

    @SerializedName("count")
    @ColumnInfo(name = "count")
    var count: Int,

    @SerializedName("ts")
    @ColumnInfo(name = "timestamp")
    var timestamp: Int
)
