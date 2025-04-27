package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.message.SendMessageDataDbModel

@Dao
interface SendMessageDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: SendMessageDataDbModel): Long

    @Query("SELECT * FROM send_message_entity WHERE `id` = :key")
    suspend fun getDataByKey(key: Long): SendMessageDataDbModel?

    @Query("DELETE FROM send_message_entity WHERE `id` = :key")
    suspend fun deleteByKey(key: Long)

    @Query("DELETE FROM send_message_entity")
    fun clear()
}
