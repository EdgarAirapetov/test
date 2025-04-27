package com.numplates.nomera3.data.network

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class PostCommentsNew(
        @SerializedName("comments")
        val comments: List<CommentEntity>
)

@Entity(tableName = "comments")
data class CommentEntity(

        @NonNull
        @PrimaryKey
        @SerializedName("id")
        @ColumnInfo(name = "id")
        var id: Long,

        @SerializedName("uid")
        @ColumnInfo(name ="user_id")
        val uid: Long,

        @ColumnInfo(name = "post_id")
        var postId: Long,

        @SerializedName("name")
        @ColumnInfo(name = "name")
        val name: String?,

        @SerializedName("number")
        @ColumnInfo(name = "number")
        val number: String?,

        @SerializedName("avatar")
        @ColumnInfo(name = "avatar")
        val avatar: String?,

        @SerializedName("text")
        @ColumnInfo(name = "text")
        val text: String?,

        @SerializedName("date")
        @ColumnInfo(name = "date")
        val date: Long,

        @SerializedName("resp_name")
        @ColumnInfo(name = "resp_name")
        val respName: String?,

        @SerializedName("account_color")
        @ColumnInfo(name = "account_color")
        val accountColor: Int,

        @SerializedName("account_type")
        @ColumnInfo(name = "account_type")
        val accountType: Int,

        @SerializedName("user")
        @ColumnInfo(name = "user")
        val user: UserComments
)

data class UserComments(

        @SerializedName("id")
        var userId: Long,

        @SerializedName("name")
        var name: String,

        @SerializedName("birthday")
        var birthday: Long?,

        @SerializedName("avatar")
        var avatar: String?,

        @SerializedName("account_type")
        var accountType: Int,

        @SerializedName("account_color")
        var accountColor: Int?,

        @SerializedName("gender")
        var gender: Int,

        @SerializedName("country")
        var country: String?,

        @SerializedName("city")
        var city: String?,

        @SerializedName("approved")
        var approved: Int = 0,

        @SerializedName("top_content_maker")
        var topContentMaker: Int = 0
)
