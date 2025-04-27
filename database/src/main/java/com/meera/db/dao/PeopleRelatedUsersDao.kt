package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.people.PeopleRelatedUserDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleRelatedUsersDao {

    @Query("DELETE FROM peopleRelatedUsers")
    suspend fun clear()

    @Query("DELETE FROM peopleRelatedUsers WHERE userId =:userId")
    suspend fun removeUserById(userId: Long)

    @Query("SELECT * FROM peopleRelatedUsers")
    suspend fun getRelatedUsers(): List<PeopleRelatedUserDbModel>

    @Query("UPDATE peopleRelatedUsers SET friend_request =:isAddToFriendRequest WHERE userId =:userId")
    suspend fun updateHasFriendRequest(userId: Long, isAddToFriendRequest: Boolean)

    @Query("SELECT * FROM peopleRelatedUsers")
    fun observeRelatedUsers(): Flow<List<PeopleRelatedUserDbModel>>

    @Query("SELECT * FROM peopleRelatedUsers WHERE userId =:userId")
    suspend fun getRelatedUserById(userId: Long) : PeopleRelatedUserDbModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelatedUsers(users: List<PeopleRelatedUserDbModel>)
}
