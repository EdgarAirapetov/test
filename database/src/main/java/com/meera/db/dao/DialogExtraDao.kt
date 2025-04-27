package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.DialogExtraEntity
import com.meera.db.models.ROOMS_EXTRA_ID
import com.meera.db.models.ROOMS_EXTRA_LAST_INPUT_TEXT
import com.meera.db.models.ROOMS_EXTRA_TABLE_NAME

@Dao
abstract class DialogExtraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(dialog: DialogExtraEntity) : Long

    @Query("UPDATE $ROOMS_EXTRA_TABLE_NAME SET $ROOMS_EXTRA_LAST_INPUT_TEXT = :lastMessage WHERE $ROOMS_EXTRA_ID = :roomId")
    abstract fun updateLastTextMessage(lastMessage: String, roomId: Long) : Int

    @Query("SELECT $ROOMS_EXTRA_LAST_INPUT_TEXT FROM $ROOMS_EXTRA_TABLE_NAME WHERE $ROOMS_EXTRA_ID = :roomId")
    abstract fun getLastTextMessage(roomId: Long) : String

}
