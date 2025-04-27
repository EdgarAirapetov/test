package com.meera.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.MediakeyboardFavoriteDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MediakeyboardFavoritesDao {

    @Query("SELECT * FROM mediakeyboard_favorite WHERE isFromMoments = :isForMoments")
    fun getAllFavoritesFlow(isForMoments: Boolean): Flow<List<MediakeyboardFavoriteDbModel>>

    @Query("SELECT * FROM mediakeyboard_favorite WHERE isFromMoments = :isForMoments ORDER BY id DESC")
    fun getAllFavorites(isForMoments: Boolean): DataSource.Factory<Int, MediakeyboardFavoriteDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: MediakeyboardFavoriteDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteList(favoriteList: List<MediakeyboardFavoriteDbModel>)

    @Query("DELETE FROM mediakeyboard_favorite WHERE id = :favoriteId")
    suspend fun deleteFavorite(favoriteId: Int)

    @Query("DELETE FROM mediakeyboard_favorite")
    suspend fun deleteAllFavorites()

}
