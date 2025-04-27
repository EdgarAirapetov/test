package com.numplates.nomera3.data.network

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationMeta(

        @SerializedName("post_id")
        @ColumnInfo(name = "post_id")
        var postId: Long?,

        @SerializedName("comment_id")
        @ColumnInfo(name = "comment_id")
        var commentId: Long?,

        @SerializedName("comment")
        @ColumnInfo(name = "comment")
        var comment: String?,

        @SerializedName("gift_id")
        @ColumnInfo(name = "gift_id")
        var giftId: Long?,

        @SerializedName("image")
        @ColumnInfo(name = "image")
        var image: String?,

        @SerializedName("title")
        @ColumnInfo(name = "title")
        var title: String?,

        @SerializedName("group_id")
        @ColumnInfo(name = "group_id")
        var groupId: Long?,

        @SerializedName("room_id")
        var roomId: Long?

        /*@SerializedName("is_anonym")
        var isAnonymous: Int?*/

): Serializable