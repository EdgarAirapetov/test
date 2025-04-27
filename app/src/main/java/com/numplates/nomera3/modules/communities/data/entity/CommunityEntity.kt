package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.Author
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class CommunityEntity(
    @SerializedName("group_id")
    var groupId: Int,

    @SerializedName("name")
    var name: String?,

    @SerializedName("description")
    var description: String?,

    @SerializedName("avatar")
    var avatar: String?,

    @SerializedName("avatar_big")
    var avatarBig: String?,

    @SerializedName("private")
    var private: Int,

    @SerializedName("royalty")
    var royalty: Int,

    @SerializedName("moderation")
    var moderation: Int,

    @SerializedName("is_author")
    var isAuthor: Int,

    @SerializedName("is_moderator")
    var isModerator: Int,

    @SerializedName("time_created")
    var timeCreated: Long,

    @SerializedName("country_id")
    var countryId: Int,

    @SerializedName("users")
    var users: Int,

    @SerializedName("blocked_users")
    var blockedUsers: Int,

    @SerializedName("user_id")
    var userId: Long,

    @SerializedName("author_check")
    var authorCheck: Int,

    @SerializedName("posts")
    var posts: Int,

    @SerializedName("joined_users")
    var joinedUsers: Int,

    @SerializedName("is_subscribed")
    var isSubscribed: Int,

    @SerializedName("user_status")
    var userStatus: Int,

    @SerializedName("author")
    var author: Author?,

    @SerializedName("subscribed_notifications")
    var subscribedNotifications: Int?

) : Serializable, ProfileListItem {

    override val caption: String?
        get() = name

    override val imageUrl: String?
        get() = avatar

    override val num: String?
        get() = users.toString()

    companion object {
        const val USER_STATUS_NOT_SENT = 0
        const val USER_STATUS_UNSUBSCRIBED = 1
        const val USER_STATUS_NOT_YET_APPROVED = 2
        const val USER_STATUS_DECLINED = 3
        const val USER_STATUS_APPROVED = 4
        const val USER_STATUS_BANNED = 5
    }

}