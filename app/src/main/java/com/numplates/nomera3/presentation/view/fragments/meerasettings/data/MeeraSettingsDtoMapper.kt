package com.numplates.nomera3.presentation.view.fragments.meerasettings.data

import com.numplates.nomera3.data.network.Exclusions
import com.numplates.nomera3.data.network.MessageEclusions
import com.numplates.nomera3.data.network.PushSettingsResponse
import com.numplates.nomera3.data.network.SubscriptionExclusions
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.ExclusionsModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.MessageExclusionsModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.SubscriptionExclusionsModel
import javax.inject.Inject

class MeeraSettingsDtoMapper @Inject constructor() {

    fun mapPushSettingsDtoToModel(data: PushSettingsResponse): PushSettingsModel {
        return PushSettingsModel(
            notificationsEnabled = data.notificationsEnabled,
            notificationsPostComments = data.notificationsPostComments,
            notificationsMomentComments = data.notificationsMomentComments,
            notificationsCallUnavailable = data.notificationsCallUnavailable,
            notificationsChatRequest = data.notificationsChatRequest,
            notificationsFriendRequest = data.notificationsFriendRequest,
            notificationsGalleryReaction = data.notificationsGalleryReaction,
            notificationsGifts = data.notificationsGifts,
            notificationsGroupComment = data.notificationsGroupComment,
            notificationsGroupJoin = data.notificationsGroupJoin,
            notificationsMessages = data.notificationsMessages,
            notificationsMomentCommentReaction = data.notificationsMomentCommentReaction,
            notificationsMomentCommentsReply = data.notificationsMomentCommentsReply,
            notificationsMomentReaction = data.notificationsMomentReaction,
            notificationsPostCommentReaction = data.notificationsPostCommentReaction,
            notificationsPostCommentsReply = data.notificationsPostCommentsReply,
            notificationsPostReaction = data.notificationsPostReaction,
            notificationsShowComments = data.notificationsShowComments,
            notificationsShowMessageInPush = data.notificationsShowMessageInPush,
            commentsMention = data.commentsMention,
            exclusions = mapToExclusionsModel(data.exclusions),
            friendsBirthday = data.friendsBirthday,
            globalMention = data.globalMention,
            postMention = data.postMention,
            groupChatMention = data.groupChatMention
        )
    }

    fun mapPushSettingsModelToDto(settings: PushSettingsModel): PushSettingsResponse {
        return PushSettingsResponse(
        notificationsEnabled = settings.notificationsEnabled,
        notificationsPostComments = settings.notificationsPostComments,
        notificationsMomentComments = settings.notificationsMomentComments,
        notificationsPostCommentReaction = settings.notificationsPostCommentReaction,
        notificationsMomentCommentReaction = settings.notificationsMomentCommentReaction,
        notificationsPostReaction = settings.notificationsPostReaction,
        notificationsMomentReaction = settings.notificationsMomentReaction,
        notificationsGalleryReaction = settings.notificationsGalleryReaction,
        notificationsPostCommentsReply = settings.notificationsPostCommentsReply,
        notificationsMomentCommentsReply = settings.notificationsMomentCommentsReply,
        notificationsFriendRequest = settings.notificationsFriendRequest,
        notificationsGifts = settings.notificationsGifts,
        notificationsGroupComment = settings.notificationsGroupComment,
        notificationsGroupJoin = settings.notificationsGroupJoin,
        notificationsMessages = settings.notificationsMessages,
        notificationsShowMessageInPush = settings.notificationsShowMessageInPush,
        notificationsShowComments = settings.notificationsShowComments,
        exclusions = mapToExclusions(settings.exclusions),
        groupChatMention = settings.groupChatMention,
        postMention = settings.postMention,
        commentsMention = settings.commentsMention,
        globalMention = settings.globalMention,
        friendsBirthday = settings.friendsBirthday,
        notificationsChatRequest = settings.notificationsChatRequest,
        notificationsCallUnavailable = settings.notificationsCallUnavailable
        )
    }

    private fun mapToExclusionsModel(exclusions: Exclusions?): ExclusionsModel? {
        val messageExcl = MessageExclusionsModel(
            count = exclusions?.messageExclusions?.count
        )
        val subscriptionExcl = SubscriptionExclusionsModel(
            count = exclusions?.subscriptionExclusions?.count,
            showNotificationLevel = exclusions?.subscriptionExclusions?.showNotificationLevel
        )
        return ExclusionsModel(
            messageExclusions = messageExcl,
            subscriptionExclusions = subscriptionExcl
        )
    }

    private fun mapToExclusions(exclusionsModel: ExclusionsModel?): Exclusions {
        val messageExclusions = MessageEclusions(count = exclusionsModel?.messageExclusions?.count)
        val subscriptionExclusions = SubscriptionExclusions(
            count = exclusionsModel?.subscriptionExclusions?.count,
            showNotificationLevel = exclusionsModel?.subscriptionExclusions?.showNotificationLevel
        )
        return Exclusions(
            messageExclusions = messageExclusions,
            subscriptionExclusions = subscriptionExclusions
        )
    }
}
