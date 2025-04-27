package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PushSettingsResponse(
        @SerializedName("notif")
        var notificationsEnabled: Boolean?,

        @SerializedName("notif_comment")
        var notificationsPostComments: Boolean?,

        @SerializedName("notif_moment_comment")
        var notificationsMomentComments: Boolean?,

        @SerializedName("notif_comment_reaction")
        var notificationsPostCommentReaction: Boolean?,

        @SerializedName("notif_moment_comment_reaction")
        var notificationsMomentCommentReaction: Boolean?,

        @SerializedName("notif_post_reaction")
        var notificationsPostReaction: Boolean?,

        @SerializedName("notif_moment_reaction")
        var notificationsMomentReaction: Boolean?,

        @SerializedName("notif_gallery_reaction")
        var notificationsGalleryReaction: Boolean?,

        @SerializedName("notif_comment_reply")
        var notificationsPostCommentsReply: Boolean?,

        @SerializedName("notif_moment_comment_reply")
        var notificationsMomentCommentsReply: Boolean?,

        @SerializedName("notif_friend_request")
        var notificationsFriendRequest: Boolean?,

        @SerializedName("notif_gift")
        var notificationsGifts: Boolean?,

        @SerializedName("notif_group_comment")
        var notificationsGroupComment: Boolean?,

        @SerializedName("notif_group_join")
        var notificationsGroupJoin: Boolean?,

        @SerializedName("notif_msg")
        var notificationsMessages: Boolean?,

        @SerializedName("show_message_text")
        var notificationsShowMessageInPush: Boolean?,

        @SerializedName("show_comment_text")
        var notificationsShowComments: Boolean?,

        @SerializedName("exclusions")
        var exclusions: Exclusions?,

        // mentions
        @SerializedName("notif_group_chat_mention")
        var groupChatMention: Boolean?,

        @SerializedName("notif_post_mention")
        var postMention: Boolean?,

        @SerializedName("notif_comment_mention")
        var commentsMention: Boolean?,

        @SerializedName("notif_mention")
        var globalMention: Boolean?,

        @SerializedName("notif_birthday")
        var friendsBirthday: Boolean?,

        @SerializedName("notif_chat_request")
        var notificationsChatRequest: Boolean?,

        @SerializedName("notif_call_unavailable")
        var notificationsCallUnavailable: Boolean?,

) : Parcelable

@Parcelize
data class Exclusions(
        @SerializedName("messages")
        var messageExclusions: MessageEclusions?,

        @SerializedName("subscriptions")
        var subscriptionExclusions: SubscriptionExclusions?
) : Parcelable


@Parcelize
data class MessageEclusions(
        @SerializedName("count")
        var count: Int?
) : Parcelable


@Parcelize
data class SubscriptionExclusions(
        @SerializedName("count")
        var count: Int?,
        // show / hide (friends subscriptions)
        @SerializedName("show_notification_level")
        val showNotificationLevel: Int?
) : Parcelable
