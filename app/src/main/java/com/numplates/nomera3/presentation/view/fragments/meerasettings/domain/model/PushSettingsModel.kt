package com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model


data class PushSettingsModel(
    val notificationsEnabled: Boolean?,
    val notificationsPostComments: Boolean?,
    val notificationsMomentComments: Boolean?,
    val notificationsPostCommentReaction: Boolean?,
    val notificationsMomentCommentReaction: Boolean?,
    val notificationsPostReaction: Boolean?,
    val notificationsMomentReaction: Boolean?,
    val notificationsGalleryReaction: Boolean?,
    val notificationsPostCommentsReply: Boolean?,
    val notificationsMomentCommentsReply: Boolean?,
    val notificationsFriendRequest: Boolean?,
    val notificationsGifts: Boolean?,
    val notificationsGroupComment: Boolean?,
    val notificationsGroupJoin: Boolean?,
    val notificationsMessages: Boolean?,
    val notificationsShowMessageInPush: Boolean?,
    val notificationsShowComments: Boolean?,
    val exclusions: ExclusionsModel?,
    val groupChatMention: Boolean?,
    val postMention: Boolean?,
    val commentsMention: Boolean?,
    val globalMention: Boolean?,
    val friendsBirthday: Boolean?,
    val notificationsChatRequest: Boolean?,
    val notificationsCallUnavailable: Boolean?,
)

data class ExclusionsModel(
    val messageExclusions: MessageExclusionsModel,
    val subscriptionExclusions: SubscriptionExclusionsModel?
)

data class MessageExclusionsModel(
    val count: Int?
)

data class SubscriptionExclusionsModel(
    val count: Int?,
    val showNotificationLevel: Int?
)
