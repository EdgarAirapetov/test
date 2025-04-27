package com.numplates.nomera3.data.fcm

import com.google.gson.annotations.SerializedName
import com.meera.core.extensions.empty
import com.meera.db.models.dialog.UserChat

data class PushObjectNew(

    @SerializedName("type")
    val type: String?,

    // @SerializedName("notify_id")
    val notifyId: Int? = 0,                  // Auto generate notification Id

    @SerializedName("user")
    val user: UserChat? = UserChat(),

    @SerializedName("room_id")
    val roomId: Long? = 0,

    @SerializedName("post_id")
    val postId: Long? = 0,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("description")
    val description: String,

    @SerializedName("user_id")
    val userId: Long? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("group_id")
    val groupId: Int? = null,

    @SerializedName("event_group_id")
    val eventGroupId: String? = null,

    @SerializedName("message_id")
    val messageId: String? = "",

    @SerializedName("sender_id")
    val senderId: Long? = null,

    @SerializedName("sender_avatar")
    val senderAvatar: String? = null,

    @SerializedName("url")
    var url: String? = "",

    @SerializedName("image_url")
    var imageUrl: String? = String.empty(),

    @SerializedName("comment_id")
    var commentId: Long?,

    @SerializedName("event_id")
    var eventId: String? = null,

    @SerializedName("last_reaction")
    var lastReaction: String? = null,

    @SerializedName("chat_name")
    val chatName: String? = null,

    @SerializedName("user_name")
    val userName: String? = null,

    @SerializedName("is_group_chat")
    val isGroupChat: Boolean? = null,

    @SerializedName("isResended")
    val isResended: Boolean? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("moment_id")
    var momentId: Long? = null,

    @SerializedName("moment_author_id")
    var momentAuthorId: Long? = null,

    @SerializedName("attachment_url")
    val attachmentUrl: String? = null,

    @SerializedName("show_reply")
    val showReply: Int? = null

) {
    constructor(
        title: String,
        description: String,
        roomId: Long?,
        type: String?,
        user: UserChat?,
        commentId: Long?
    ) : this(type, 0, user, roomId, 0, title, description, commentId = commentId)

    companion object {

        fun unsentMessages(
            roomId: Long?,
            title: String,
            description: String
        ): PushObjectNew {
            return PushObjectNew(
                title = title,
                description = description,
                roomId = roomId,
                type = String.empty(),
                user = null,
                commentId = 0L
            )
        }

    }

}
