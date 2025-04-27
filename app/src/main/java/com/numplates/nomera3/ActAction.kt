package com.numplates.nomera3

import android.content.Intent
import android.content.res.AssetManager
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactionsParams
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.presentation.view.utils.apphints.Hint

sealed interface ActActions {
    object LoadSignupCountries : ActActions
    object ResetSubscriptionsRoad : ActActions
    object TryToRegisterShakeEvent : ActActions
    object UnregisterShakeEventListener : ActActions
    object SubscribeEvent : ActActions
    object LoadPrivacySettings : ActActions
    object ObserveRefreshTokenRestService : ActActions
    object OnShowReactionBubble : ActActions
    object HandleLocationEnableClicked : ActActions
    object GetRooms : ActActions
    object UpdateBirthdayDialogShown : ActActions
    object ShowCalendarIfNot : ActActions
    object GetUnreadBadgeInfo : ActActions
    object RequestCounter : ActActions
    object SubscribeProfileNotification : ActActions
    object StartReceivingLocationUpdates : ActActions
    object ConnectSocket : ActActions
    object StopReceivingLocationUpdates : ActActions
    object ShowAllPosts : ActActions
    object DisconnectWebSocket : ActActions
    object LogBack : ActActions
    object LogSwipeBack : ActActions
    object ShowBirthdayDialogDelayed : ActActions
    object ShowDialogIfBirthday : ActActions
    object PushBirthdayState : ActActions
    class InitializeAvatarSDK(val assets: AssetManager) : ActActions
    class MarkHintsAsShown(val hint: Hint) : ActActions
    class UpdateUnreadNotificationBadge(val needToShow: Boolean) : ActActions
    class SetNeedToShowUpdateAppMark(val isNeedToShowUpdateAppMark: Boolean) : ActActions
    class HandleDeepLinks(val intent: Intent?) : ActActions
    class TriggerGoToChat(val roomId: Long) : ActActions
    class UpdateGalleryPost(val path: String?) : ActActions
    class MarkAsRead(val pushEventId: String, val isGroup: Boolean) : ActActions
    class AddReaction(
        val reactionSource: ReactionSource,
        val currentReactionList: List<ReactionEntity>,
        val reaction: ReactionType,
        val reactionsParams: AmplitudeReactionsParams?,
        val isFromBubble: Boolean = false
    ) : ActActions

    class AddReactionMeera(
        val reactionSource: MeeraReactionSource,
        val currentReactionList: List<ReactionEntity>,
        val reaction: ReactionType,
        val reactionsParams: AmplitudeReactionsParams?,
        val isFromBubble: Boolean = false
    ) : ActActions

    class RemoveReaction(
        val reactionSource: ReactionSource,
        val currentReactionList: List<ReactionEntity>,
        val reactionToRemove: ReactionType,
        val reactionsParams: AmplitudeReactionsParams?
    ) : ActActions

    class RemoveReactionMeera(
        val reactionSource: MeeraReactionSource,
        val currentReactionList: List<ReactionEntity>,
        val reactionToRemove: ReactionType,
        val reactionsParams: AmplitudeReactionsParams?
    ) : ActActions
    object UpdatePeopleBadge : ActActions

    object OnScreenshotTaken : ActActions

    object LogOpenCommunityFromDeeplink : ActActions

    data object StartListeningSyncNotificationService: ActActions
    data object StopListeningSyncNotificationService: ActActions
}
