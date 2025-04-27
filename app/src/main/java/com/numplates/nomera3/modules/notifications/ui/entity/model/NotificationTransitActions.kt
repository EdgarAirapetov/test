package com.numplates.nomera3.modules.notifications.ui.entity.model

import android.view.View
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.reaction.data.ReactionType

sealed class NotificationTransitActions(
    var notifId: String? = null,
    var isGroup: Boolean? = null
) {

    class OnTransitToMomentScreen(
        val userId: Long,
        val momentScreenOpenAnimationView: View?,
        val momentId: Long? = null,
        val pushInfo: String? = null,
        val latestReactionType: ReactionType? = null,
        val commentId: Long? = null,
        val hasNewMoments: Boolean? = null
    ) : NotificationTransitActions()

    class OnTransitToPostScreen(
        val postId: Long?,
        val pushInfo: String? = null,
        val latestReactionType: ReactionType? = null
    ) : NotificationTransitActions()

    class OnTransitToCommentPostScreen(val postId: Long?, val commentId: Long?) : NotificationTransitActions()
    class OnTransitToCommentPostScreenWithReactions(
        val postId: Long?,
        val commentId: Long?,
        val latestReactionType: ReactionType? = null
    ) : NotificationTransitActions()

    class OnTransitToProfileViewScreen(val postId: Long?) : NotificationTransitActions()
    object OnTransitToIncomingFriendRequestScreen : NotificationTransitActions()
    class OnTransitToUserProfileScreen(val user: User?) : NotificationTransitActions()
    object OnTransitToGiftScreen : NotificationTransitActions()
    class OnTransitToGroupChat(val roomId: Long?) : NotificationTransitActions()
    class OnSystemNotification(val uri: String?) : NotificationTransitActions()
    class OnTransitToPrivateGroupRequest(val groupId: Long?) : NotificationTransitActions()
    class OnUserAvatarClicked(val user: User?, val view: View?) : NotificationTransitActions()
    class OnBirthdayFriendClicked(val userId: Long?, val userName: String?, val birthday: Long) :
        NotificationTransitActions()
    class OnBirthdayUserClicked(val user: User?) : NotificationTransitActions()
    class OnCreateAvatarClicked : NotificationTransitActions()
    class OnUserSoftBlockClicked(
        val isBlocked: Boolean,
        val blockReason: String,
        val blockedTo: Long
    ) : NotificationTransitActions() {
        override fun toString(): String {
            return "IsBlocked:$isBlocked Reason:$blockReason BlockedTo:$blockedTo"
        }
    }

    class OnTransitToChatScreen(val user: User?) : NotificationTransitActions()

    class OnCommunityNotificationIconClicked(val communityId: Int) : NotificationTransitActions()

    class OnTransitGroupNotificationsScreen(
        val notificationId: String,
        val type: String,
        val isRead: Boolean
    ) : NotificationTransitActions()

    class OnTransitToEventView(
        val postId: Long
    ) : NotificationTransitActions()
}
