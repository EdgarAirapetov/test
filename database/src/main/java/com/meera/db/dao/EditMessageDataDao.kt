package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.message.EditMessageDataDbModel

@Dao
interface EditMessageDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: EditMessageDataDbModel): Long

    @Query("SELECT * FROM edit_message_entity WHERE `id` = :key")
    suspend fun getDataByKey(key: Long): EditMessageDataDbModel?

    @Query("DELETE FROM edit_message_entity WHERE `id` = :key")
    suspend fun deleteByKey(key: Long)

    @Query("DELETE FROM edit_message_entity")
    abstract fun clear()
}
