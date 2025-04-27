package com.meera.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meera.db.models.message.MessageEntity

@Entity(tableName = "draft")
data class DraftDbModel(

    @ColumnInfo(name = "room_id")
    val roomId: Long?,

    @ColumnInfo(name = "user_id")
    val userId: Long?,

    @ColumnInfo(name = "last_updated_timestamp")
    val lastUpdatedTimestamp: Long,

    @ColumnInfo(name = "text")
    val text: String?,

    @ColumnInfo(name = "reply")
    val reply: MessageEntity?
) {

    @PrimaryKey(autoGenerate = true)
    var draftId: Int = 0

}
