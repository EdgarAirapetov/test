package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.people.PeopleApprovedUserDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleApprovedUsersDao {

    @Query("DELETE FROM peopleApprovedUser")
    suspend fun clear()

    @Query("SELECT * FROM peopleApprovedUser ORDER BY subscribers_count DESC")
    suspend fun getApprovedUsers(): List<PeopleApprovedUserDbModel>

    @Query("UPDATE peopleApprovedUser SET user_subscribed =:isUserSubscribed WHERE userId =:userId")
    suspend fun updateUserSubscribed(userId: Long, isUserSubscribed: Boolean)

    @Query("SELECT * FROM peopleApprovedUser")
    fun observeApprovedUsers(): Flow<List<PeopleApprovedUserDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovedUsers(approvedUsers: List<PeopleApprovedUserDbModel>)

    @Query("SELECT * FROM peopleApprovedUser WHERE userId =:userId")
    suspend fun getApprovedUserById(userId: Long) : PeopleApprovedUserDbModel
}
