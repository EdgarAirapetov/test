package com.meera.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mediakeyboard_favorite")
data class MediakeyboardFavoriteDbModel(
    @PrimaryKey
    val id: Int,
    val type: String,
    val url: String,
    val preview: String,
    val duration: Int?,
    val ratio: Float?,
    val lottieUrl: String?,
    val webpUrl: String?,
    val emoji: String?,
    val favoriteId: Long?,
    val stickerId: Int?,
    val messageId: String?,
    val gifId: String?,
    val stickerPackTitle: String?,
    val isFromMoments: Boolean
)
