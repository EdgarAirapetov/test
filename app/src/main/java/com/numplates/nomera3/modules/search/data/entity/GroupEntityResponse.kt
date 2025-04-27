package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName

data class GroupEntityResponse(

        @SerializedName("group_id")
        val groupId: Int?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("description")
        val description: String?,

        @SerializedName("avatar")
        val avatar: String?,

        @SerializedName("avatar_big")
        val avatarBig: String?,

        @SerializedName("private")
        val private: Int?,

        @SerializedName("royalty")
        val royalty: Int?,

        @SerializedName("moderation")
        val moderation: Int?,

        @SerializedName("is_author")
        val isAuthor: Int?,

        @SerializedName("is_moderator")
        val isModerator: Int?,

        @SerializedName("time_created")
        val timeCreated: Long?,

        @SerializedName("country_id")
        val countryId: Int?,

        @SerializedName("users")
        val users: Int?,

        @SerializedName("user_id")
        val userId: Long?,

        @SerializedName("author_check")
        val authorCheck: Int?,

        @SerializedName("posts")
        val posts: Int?,

        @SerializedName("joined_users")
        val joinedUsers: Int?,

        @SerializedName("is_subscribed")
        val isSubscribed: Int?,

        @SerializedName("user_status")
        val userStatus: Int?,

        @SerializedName("author")
        val author: GroupAuthorEntityResponse?
)
