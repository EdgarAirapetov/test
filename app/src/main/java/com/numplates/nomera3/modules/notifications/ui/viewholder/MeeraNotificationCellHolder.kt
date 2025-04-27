package com.numplates.nomera3.modules.notifications.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.UiKitNotificationCellView
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.numplates.nomera3.data.fcm.IPushInfo
import com.numplates.nomera3.modules.notifications.ui.entity.MentionNotificationType
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions

class MeeraNotificationCellHolder(view: View): RecyclerView.ViewHolder(view) {

    private val cellView = view as UiKitNotificationCellView

    fun bind(
        item: NotificationCellUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        cellView.setConfig(item.config)
        cellView.setOnActionButtonClickListener {
            handleActionButtonClicks(item.data, onClickNotification)
        }
        cellView.setOnAvatarClickListener { avatarView ->
            handleUserAvatarClick(item, avatarView, onClickNotification)
        }
        cellView.setOnImageClickListener {
            handleItemClick(item, onClickNotification)
        }
        itemView.setThrottledClickListener {
            handleItemClick(item, onClickNotification)
        }
    }

    private fun handleActionButtonClicks(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        when (item.type) {
            IPushInfo.FRIEND_CONFIRM -> {
                val action = NotificationTransitActions.OnTransitToChatScreen(item.users[0])
                onClickNotification.invoke(action, false)
            }
            IPushInfo.GIFT_RECEIVED_NOTIFICATION -> {
                val action = NotificationTransitActions.OnTransitToGiftScreen
                onClickNotification.invoke(action, false)
            }
            IPushInfo.BIRTHDAY -> {
                val user = item.users.firstOrNull()
                val action = NotificationTransitActions
                    .OnBirthdayUserClicked(user)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
            IPushInfo.NOTIFY_PEOPLE -> {
                val action = NotificationTransitActions.OnTransitToUserProfileScreen(item.users[0])
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
        }
    }

    private fun handleUserAvatarClick(
        item: NotificationCellUiModel,
        avatarView: View,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        val notification = item.data
        val isMomentsEnabled = item.config.userPicUiModel?.storiesState != UserpicStoriesStateEnum.NO_STORIES
        if (notification.meta.isAnonym.not()) {
            val user = notification.users.firstOrNull()
            val action = if (user?.hasMoments == true && isMomentsEnabled) {
                NotificationTransitActions
                    .OnTransitToMomentScreen(
                        userId = user.userId.toLong(),
                        momentScreenOpenAnimationView = avatarView,
                        hasNewMoments = user.hasNewMoments
                    )
            } else {
                NotificationTransitActions.OnUserAvatarClicked(user, avatarView)
            }

            onClickNotification(action, false)
        }
    }

    private fun handleItemClick(
        item: NotificationCellUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        if (item.data.isGroup) {
            handleGroupedItemClick(
                item = item.data,
                onClickNotification = onClickNotification
            )
        } else {
            handleSingleItemClick(
                item = item.data,
                onClickNotification = onClickNotification
            )
        }
    }

    private fun handleGroupedItemClick(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        val action = NotificationTransitActions
            .OnTransitGroupNotificationsScreen(item.id, item.type, item.isRead).apply {
                notifId = item.id
                isGroup = true
            }

        onClickNotification.invoke(action, true)
    }

    private fun handleSingleItemClick(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        when (item.type) {
            IPushInfo.MOMENT,
            IPushInfo.MOMENT_REACTION,
            IPushInfo.MOMENT_COMMENT,
            IPushInfo.MOMENT_MENTION_COMMENT,
            IPushInfo.MOMENT_COMMENT_REACTION,
            IPushInfo.MOMENT_COMMENT_REPLY,
            MentionNotificationType.MOMENT_MENTION_COMMENT.value,
            -> {
                val userId = item.meta.momentAuthorId ?: return
                val momentId = item.meta.momentId
                val hasNewMoments = item.users.firstOrNull()?.hasNewMoments
                val transitPushInfo =
                    if (item.type == IPushInfo.MOMENT_REACTION) item.type else null
                val action = NotificationTransitActions
                    .OnTransitToMomentScreen(
                        userId = userId,
                        momentId = momentId,
                        pushInfo = transitPushInfo,
                        momentScreenOpenAnimationView = null,
                        latestReactionType = item.meta.reaction,
                        commentId = item.meta.commentId,
                        hasNewMoments = hasNewMoments
                    )
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }
            MentionNotificationType.MENTION_POST.value,
            MentionNotificationType.MENTION_EVENT_POST.value,
            IPushInfo.COMMUNITY_NEW_POST,
            IPushInfo.POST_REACTION,
            IPushInfo.SUBSCRIBERS_POST_CREATE -> {
                val transitPushInfo = if (item.type == IPushInfo.POST_REACTION) item.type else null
                val action = NotificationTransitActions
                    .OnTransitToPostScreen(item.meta.postId, transitPushInfo, item.meta.reaction)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            MentionNotificationType.MENTION_COMMENT.value,
            MentionNotificationType.MENTION_COMMENT_YOUR.value,
            MentionNotificationType.MENTION_COMMENT_GROUP.value,
            MentionNotificationType.MENTION_COMMENT_GROUP_YOUR.value,
            IPushInfo.POST_COMMENT_YOUR,
            IPushInfo.POST_COMMENT,
            IPushInfo.POST_COMMENT_REPLY,
            IPushInfo.GROUP_COMMENT_YOUR,
            IPushInfo.GROUP_COMMENT,
            IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW,
            IPushInfo.GROUP_COMMENT_REPLY -> {
                val action =
                    NotificationTransitActions
                        .OnTransitToCommentPostScreen(item.meta.postId, item.meta.commentId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.COMMENT_REACTION -> {
                val action =
                    NotificationTransitActions
                        .OnTransitToCommentPostScreenWithReactions(
                            postId = item.meta.postId,
                            commentId = item.meta.commentId,
                            latestReactionType = item.meta.reaction
                        )
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.GALLERY_REACTION -> {
                val action = NotificationTransitActions
                    .OnTransitToProfileViewScreen(item.meta.postId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
            IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE -> {
                val action = NotificationTransitActions
                    .OnTransitToPostScreen(item.meta.postId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }

            IPushInfo.FRIEND_REQUEST -> {
                val action = NotificationTransitActions
                    .OnTransitToIncomingFriendRequestScreen
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.FRIEND_CONFIRM,
            IPushInfo.NOTIFY_PEOPLE -> {
                val action = NotificationTransitActions
                    .OnTransitToUserProfileScreen(item.users[0])
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }

            IPushInfo.GIFT_RECEIVED_NOTIFICATION -> {
                val action = NotificationTransitActions.OnTransitToGiftScreen
                    .apply {
                        notifId = item.id
                        item.isGroup
                    }

                onClickNotification(action, false)
            }

            MentionNotificationType.MENTION_GROUP_CHAT.value,
            IPushInfo.ADD_TO_GROUP_CHAT -> {
                val action = NotificationTransitActions
                    .OnTransitToGroupChat(item.meta.roomId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.SYSTEM_NOTIFICATION -> {
                val action = NotificationTransitActions
                    .OnSystemNotification(item.meta.link)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.PUSH_GROUP_REQUEST -> {
                val action = NotificationTransitActions
                    .OnTransitToPrivateGroupRequest(item.meta.groupId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.EVENT_CALL_UNAVAILABLE -> {
                val action = NotificationTransitActions
                    .OnTransitToUserProfileScreen(item.users[0])
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
            IPushInfo.CREATE_ANIMATED_AVATAR -> {
                val action = NotificationTransitActions.OnCreateAvatarClicked()
                action.notifId = item.id
                onClickNotification.invoke(action, false)
            }
            IPushInfo.BIRTHDAY -> {
                val user = item.users.firstOrNull()
                val action = NotificationTransitActions
                    .OnBirthdayUserClicked(user)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
            IPushInfo.USER_SOFT_BLOCKED -> {
                val blockedTo = item.meta.userBlockedTo ?: 0L
                val action = NotificationTransitActions.OnUserSoftBlockClicked(
                    isBlocked = blockedTo > System.currentTimeMillis() / 1000,
                    blockReason = item.meta.userBlocReason ?: String.empty(),
                    blockedTo = item.meta.userBlockedTo ?: 0
                )
                onClickNotification.invoke(action, false)
            }
            IPushInfo.EVENT_START_SOON -> {
                if (item.meta.postId != null) {
                    val action = NotificationTransitActions
                        .OnTransitToEventView(item.meta.postId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }
                    onClickNotification.invoke(action, false)
                }
            }
            IPushInfo.EVENT_PARTICIPANT -> {
                if (item.meta.postId != null) {
                    val action = NotificationTransitActions
                        .OnTransitToPostScreen(item.meta.postId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }
                    onClickNotification.invoke(action, false)
                }
            }
        }
    }


}
