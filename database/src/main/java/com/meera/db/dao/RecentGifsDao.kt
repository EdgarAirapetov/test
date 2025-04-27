package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.RecentGifEntity

private const val RECENT_GIFS_COUNT = 30

@Dao
interface RecentGifsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addGifToRecent(gifEntity: RecentGifEntity): Long

    @Query("SELECT * FROM recent_gifs ORDER BY id DESC LIMIT $RECENT_GIFS_COUNT")
    fun getRecentGifs(): List<RecentGifEntity>
}
