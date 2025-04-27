package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.DraftDbModel

@Dao
interface DraftsDao {

    @Query("SELECT * FROM draft")
    suspend fun getAllDrafts(): List<DraftDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draftDbModel: DraftDbModel)

    @Query("DELETE FROM draft WHERE room_id = :roomId")
    suspend fun deleteDraft(roomId: Long?)

    @Query("DELETE FROM draft")
    suspend fun deleteAllDrafts()

}
