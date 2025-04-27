package com.meera.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


const val DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE = 1.5

@Entity(
    tableName = "recent_gifs",
    indices = [Index(value = ["small_url"], unique = true)]
)
data class RecentGifEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String = "",

    @ColumnInfo(name = "small_url")
    var smallUrl: String = "",

    @ColumnInfo(name = "original_url")
    var originalUrl: String = "",

    @ColumnInfo(name = "original_aspect_ratio")
    var originalAspectRatio: Double = DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0L
)


