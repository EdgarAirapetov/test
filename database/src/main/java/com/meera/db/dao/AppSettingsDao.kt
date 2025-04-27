package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.AppSettingsEntity

@Dao
interface AppSettingsDao {

    @Query("SELECT last_updated_dialogs FROM app_settings WHERE id = 0")
    fun getLastUpdatedDialogs() : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET last_updated_dialogs = :lastUpdated WHERE id = 0")
    fun setLastUpdatedDialogs(lastUpdated: Long)

    @Query("DELETE FROM app_settings")
    fun purge()

}
