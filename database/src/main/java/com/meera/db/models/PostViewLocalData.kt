package com.meera.db.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "viewed_posts")
data class PostViewLocalData(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "post_id")
    val postId: Long = 0L,
    @ColumnInfo(name = "feature_id")
    val featureId: Long = 0L,
    @ColumnInfo(name = "post_user_id")
    val postUserId: Long,
    @ColumnInfo(name = "group_id")
    val groupId: Int = 0,
    @ColumnInfo(name = "view_user_id")
    val viewUserId: Long = -1,
    @ColumnInfo(name = "duration")
    val viewDuration: Long = -1,
    @ColumnInfo(name = "stopViewTime")
    val stopViewTime: Long = -1,
    @ColumnInfo(name = "roadSource")
    val roadSource: String = "",
    @ColumnInfo(name = "is_feature_post")
    val isFeaturePost: Boolean = false
) {
    fun isValidPost(): Boolean {
        return postId > 0
    }

    fun isValidFeature(): Boolean {
        return featureId > 0
    }
}
