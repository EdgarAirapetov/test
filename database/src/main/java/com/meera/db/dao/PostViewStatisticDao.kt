package com.meera.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meera.db.models.PostViewLocalData

@Dao
interface PostViewStatisticDao {
    @Query("SELECT * FROM viewed_posts")
    fun getAllViewedPosts(): List<PostViewLocalData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: PostViewLocalData): Long

    @Query("UPDATE viewed_posts SET view_user_id = :userId WHERE view_user_id = -1")
    fun updateUserId(userId: Long): Int

    @Query("DELETE FROM viewed_posts WHERE post_user_id = :userId")
    fun removeUsersPostsWithUserId(userId: Long)

    @Query("DELETE FROM viewed_posts")
    fun removeAllPostsViews()
}
