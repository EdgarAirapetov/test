package com.numplates.nomera3.modules.notifications.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.MomentAsset
import com.meera.db.models.PostAsset
import com.meera.db.models.message.UniquenameEntity

data class MetaNotificationEntityResponse(

        @SerializedName("post_id")
        val postId: Long?,

        @SerializedName("post_text")
        val postText: String?,

        @SerializedName("post_asset")
        val postAsset: PostAsset?,

        @SerializedName("moment_asset")
        val momentAsset: MomentAsset?,

        @SerializedName("comment_id")
        val commentId: Long?,

        @SerializedName("group_id")
        val groupId: Long?,

        @SerializedName("group_name")
        val groupName: String?,

        @SerializedName("gift_id")
        val giftId: Long?,

        @SerializedName("comment")
        val comment: String?,

        @SerializedName("reply_comment")
        val replyComment: MetaCommentReplyNotificationDto?,

        @SerializedName("image")
        val image: String?,

        @SerializedName("room_id")
        val roomId: Long?,

        @SerializedName("title")
        val title: String?,

        @SerializedName("text")
        val text: String?,

        @SerializedName("avatar")
        val avatar: String?,

        @SerializedName("url")
        val link: String?,

        @SerializedName("message")
        val messageText: String? = null,

        @SerializedName("message_id")
        val messageId: String? = null,

        @SerializedName("community_avatar")
        val communityAvatar: String? = null,

        @SerializedName("community_id")
        val communityId: Int? = null,

        @SerializedName("community_name")
        val communityName: String? = null,

        @SerializedName("is_anonym")
        val isAnonym: Boolean = false,

        @SerializedName("from_user_id")
        val fromUserId: Long? = null,

        @SerializedName(value = "tags")
        var tags: List<UniquenameEntity>? = null,

        @SerializedName(value = "post_tags")
        var postTags: List<UniquenameEntity>? = null,

        @SerializedName(value = "comment_tags")
        var commentTags: List<UniquenameEntity>? = null,

        @SerializedName("media")
        var media: MediaEntityResponse? = null,

        @SerializedName("reaction")
        val reaction:String,

        @SerializedName("block_reason_value")
        val userBlocReason: String?,

        @SerializedName("blocked_to")
        val userBlockedTo: Long?,

        @SerializedName("moment_id")
        val momentId: Long?,

        @SerializedName("moment_author_id")
        val momentAuthorId: Long?,

        @SerializedName("event_title")
        val eventTitle: String?,

        @SerializedName("image_url")
        val eventImageUrl: String?,

        @SerializedName("has_event_on_map")
        val hasEventOnMap: Boolean?,
)

data class MetaCommentReplyNotificationDto(
    @SerializedName("comment")
    val comment: String?,

    @SerializedName("comment_tags")
    val tags: List<UniquenameEntity>? = null
)
