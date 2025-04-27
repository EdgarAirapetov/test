package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif

import android.content.Context
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData.PushSettingsDesc
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData.PushSettingsExclude
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData.PushSettingsSwitch
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData.PushSettingsTitle
import javax.inject.Inject

private const val FIRST_ITEM_TOP_MARGIN = 14

class MeeraPushNotificationSettingsUiMapper @Inject constructor(
    private val context: Context
) {
    fun mapPushSettingsModelToUiState(setting: PushSettingsModel): PushSettingsState {
        val isEnable = setting.notificationsEnabled ?: true
        val generalBlock = mapGeneralBlock(setting)
        val messageBlock = mapMessageBlock(setting, isEnable)
        val requestBlock = mapRequestBlock(setting, isEnable)
        val feedbackBlock = mapFeedbackBlock(setting, isEnable)
        val eventsBlock = mapEventsBlock(setting, isEnable)
        val mentionsBlock = mapMentionsBlock(setting, isEnable)
        val postNotifications = mapPostNotification(setting)
        return PushSettingsState(
            items = generalBlock + messageBlock + requestBlock + feedbackBlock + eventsBlock + mentionsBlock + postNotifications,
            isError = false
        )
    }

    private fun mapMentionsBlock(setting: PushSettingsModel, isEnable: Boolean): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.notification_settings_mention),
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_settings_mention_group_chat),
                title = context.getString(R.string.notification_settings_mention_group_chat),
                isChosen = setting.groupChatMention ?: false,
                action = MeeraPushNotificationSettingsAction.Mentions.GroupChatMention,
                position = CellPosition.TOP,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_settings_mention_post),
                title = context.getString(R.string.notification_settings_mention_post),
                isChosen = setting.postMention ?: false,
                action = MeeraPushNotificationSettingsAction.Mentions.PostMention,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_settings_mention_komment),
                title = context.getString(R.string.notification_settings_mention_komment),
                isChosen = setting.commentsMention ?: false,
                action = MeeraPushNotificationSettingsAction.Mentions.CommentMention,
                position = CellPosition.BOTTOM,
                isEnable = isEnable
            ),
        )
    }

    private fun mapEventsBlock(setting: PushSettingsModel, isEnable: Boolean): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.notification_events)
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.receive_friends_birthday_notification_switch_text),
                title = context.getString(R.string.receive_friends_birthday_notification_switch_text),
                isChosen = setting.friendsBirthday ?: false,
                action = MeeraPushNotificationSettingsAction.ProfileEvents.FriendsBirthday,
                position = CellPosition.ALONE,
                isEnable = isEnable
            )
        )
    }

    private fun mapFeedbackBlock(setting: PushSettingsModel, isEnable: Boolean): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.feed_back)
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.meera_new_group_comment),
                title = context.getString(R.string.meera_new_group_comment),
                isChosen = setting.notificationsGroupComment ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.CommunityComments,
                position = CellPosition.TOP,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_on_post_comments),
                title = context.getString(R.string.notification_on_post_comments),
                isChosen = setting.notificationsPostComments ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.PostComments,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_on_moment_comments),
                title = context.getString(R.string.notification_on_moment_comments),
                isChosen = setting.notificationsMomentComments ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.MomentComments,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.post_comment_reply),
                title = context.getString(R.string.post_comment_reply),
                isChosen = setting.notificationsPostCommentsReply ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.PostCommentAnswer,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.moment_comment_reply),
                title = context.getString(R.string.moment_comment_reply),
                isChosen = setting.notificationsMomentCommentsReply ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.MomentCommentAnswer,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.show_comments_text),
                title = context.getString(R.string.show_comments_text),
                isChosen = setting.notificationsShowComments ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.ShowCommentsText,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.post_reaction),
                title = context.getString(R.string.post_reaction),
                isChosen = setting.notificationsPostReaction ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.PostReaction,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.moment_reaction),
                title = context.getString(R.string.moment_reaction),
                isChosen = setting.notificationsMomentReaction ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.MomentReaction,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.post_comments_reactions),
                title = context.getString(R.string.post_comments_reactions),
                isChosen = setting.notificationsPostCommentReaction ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.PostCommentReaction,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.moment_comments_reactions),
                title = context.getString(R.string.moment_comments_reactions),
                isChosen = setting.notificationsMomentCommentReaction ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.MomentCommentReaction,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.about_me_photos_reaction),
                title = context.getString(R.string.about_me_photos_reaction),
                isChosen = setting.notificationsGalleryReaction ?: false,
                action = MeeraPushNotificationSettingsAction.Feedback.AboutMePhotoReaction,
                position = CellPosition.BOTTOM,
                isEnable = isEnable
            ),
        )
    }

    private fun mapRequestBlock(setting: PushSettingsModel, isEnable: Boolean): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.meera_general_requests)
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_new_chat),
                title = context.getString(R.string.notification_new_chat),
                isChosen = setting.notificationsChatRequest ?: false,
                action = MeeraPushNotificationSettingsAction.Request.NewDialog,
                position = CellPosition.TOP,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_incoming_calls),
                title = context.getString(R.string.notification_incoming_calls),
                isChosen = setting.notificationsCallUnavailable ?: false,
                action = MeeraPushNotificationSettingsAction.Request.Calls,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_new_add_to_friend),
                title = context.getString(R.string.notification_new_add_to_friend),
                isChosen = setting.notificationsFriendRequest ?: false,
                action = MeeraPushNotificationSettingsAction.Request.AddToFriend,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_new_add_to_community),
                title = context.getString(R.string.notification_new_add_to_community),
                isChosen = setting.notificationsGroupJoin ?: false,
                action = MeeraPushNotificationSettingsAction.Request.AddToCommunity,
                position = CellPosition.BOTTOM,
                isEnable = isEnable
            )
        )
    }

    private fun mapMessageBlock(setting: PushSettingsModel, isEnable: Boolean): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.rooms_messaging)
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.notification_on_messages),
                title = context.getString(R.string.notification_on_messages),
                isChosen = setting.notificationsMessages ?: false,
                action = MeeraPushNotificationSettingsAction.Message.NewMessage,
                position = CellPosition.TOP,
                isEnable = isEnable
            ),
            PushSettingsSwitch(
                id = context.getString(R.string.show_message_content),
                title = context.getString(R.string.show_message_content),
                isChosen = setting.notificationsShowMessageInPush ?: false,
                action = MeeraPushNotificationSettingsAction.Message.ShowTextMessage,
                position = CellPosition.MIDDLE,
                isEnable = isEnable
            ),
            PushSettingsExclude(
                id = context.getString(R.string.settings_exclude),
                title = context.getString(R.string.settings_exclude),
                action = MeeraPushNotificationSettingsAction.Message.ExceptUser(),
                position = CellPosition.BOTTOM,
                actionTitle = context.getString(R.string.general_add),
                actionTitleColor = R.color.uiKitColorForegroundLink,
                usersCount = setting.exclusions?.messageExclusions?.count ?: 0
            ),
            PushSettingsDesc(
                title = context.getString(R.string.meera_notification_settings_exclude_decs)
            )
        )
    }

    private fun mapPostNotification(setting: PushSettingsModel): List<PushSettingsData> {
        return listOf(
            PushSettingsTitle(
                title = context.getString(R.string.notification_settings_sources),
            ),
            PushSettingsExclude(
                id = context.getString(R.string.meera_notification_settings_friends_and_subs),
                title = context.getString(R.string.meera_notification_settings_friends_and_subs),
                action = MeeraPushNotificationSettingsAction.Post.FriendAndSubscriptions(),
                position = CellPosition.ALONE,
                actionTitle = context.getString(R.string.general_add),
                actionTitleColor = R.color.uiKitColorForegroundLink,
                usersCount = setting.exclusions?.subscriptionExclusions?.count ?: 0
            ),
        )
    }

    private fun mapGeneralBlock(setting: PushSettingsModel): List<PushSettingsData> {
        return listOf(
            PushSettingsSwitch(
                id = context.getString(R.string.notification_on_turn_on_push),
                title = context.getString(R.string.notification_on_turn_on_push),
                isChosen = setting.notificationsEnabled ?: false,
                action = MeeraPushNotificationSettingsAction.EnablePush,
                position = CellPosition.ALONE,
                topMargin = FIRST_ITEM_TOP_MARGIN,
                isEnable = true
            )
        )
    }
}
