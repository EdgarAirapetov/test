package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.userprofile.UserProfileNew
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile")
    suspend fun getUserProfile() : UserProfileNew?

    @Query("SELECT * FROM user_profile")
    fun getUserProfileRx() : Single<UserProfileNew>

    @Query("SELECT * FROM user_profile")
    fun getUserProfileFlow(): Flow<UserProfileNew>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserProfileNew) : Long

    @Query("DELETE FROM user_profile")
    fun purge()

}
