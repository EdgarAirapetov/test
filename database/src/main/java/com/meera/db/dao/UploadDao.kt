package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.UploadItem

@Dao
interface UploadDao {

    @Query("SELECT * FROM upload_items")
    suspend fun getUploadItems(): List<UploadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(uploadItem: UploadItem): Long

    @Query("DELETE FROM upload_items WHERE id = :id")
    suspend fun remove(id: Int)
}
