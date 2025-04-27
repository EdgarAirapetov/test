package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.meera.db.models.usersettings.PrivacySettingDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacySettingsDao {

    @Query("SELECT * FROM privacy_settings")
    fun getAllFlow(): Flow<List<PrivacySettingDbModel>>

    @Query("SELECT * FROM privacy_settings")
    suspend fun getAll(): List<PrivacySettingDbModel>

    @Query("SELECT * FROM privacy_settings WHERE key = :key")
    suspend fun getByKey(key: String): PrivacySettingDbModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<PrivacySettingDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: PrivacySettingDbModel)

    @Query("UPDATE privacy_settings SET value = :value WHERE key = :key")
    suspend fun updateValue(key: String, value: Int?)

    @Query("UPDATE privacy_settings SET count_blacklist = :value WHERE key = :key")
    suspend fun updateCountBlackList(key: String, value: Int?)

    @Query("UPDATE privacy_settings SET count_whitelist = :value WHERE key = :key")
    suspend fun updateCountWhiteList(key: String, value: Int?)

    @Transaction
    suspend fun updateValuesTransaction(items: Map<String, Int?>) {
        items.forEach { (key, value) -> updateValue(key, value) }
    }

    @Delete
    suspend fun delete(setting: PrivacySettingDbModel)

    @Query("DELETE FROM privacy_settings")
    suspend fun deleteAll()
}
