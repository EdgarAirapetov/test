package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.RegistrationCountryDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistrationCountriesDao {

    @Query("SELECT * FROM registration_country")
    fun getAllRegistrationCountries(): Flow<List<RegistrationCountryDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrationCountries(countries: List<RegistrationCountryDbModel>)

    @Query("DELETE FROM registration_country")
    suspend fun deleteAllRegistrationCountries()

}
