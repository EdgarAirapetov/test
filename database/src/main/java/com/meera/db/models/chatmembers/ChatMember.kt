package com.meera.db.models.chatmembers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "chat_member")
data class ChatMember(

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    var userId: Long,

    @ColumnInfo(name = "room_id")
    var roomId: Long?,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    val type: String,

    @SerializedName("user")
    @ColumnInfo(name = "user_entity")
    val user: UserEntity
)
