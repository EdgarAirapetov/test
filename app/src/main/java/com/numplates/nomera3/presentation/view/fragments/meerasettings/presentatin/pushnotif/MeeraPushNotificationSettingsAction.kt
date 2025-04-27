package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif

import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData

sealed interface MeeraPushNotificationSettingsAction {

    data object OnViewCreated: MeeraPushNotificationSettingsAction
    data object EnablePush : MeeraPushNotificationSettingsAction

    data object ShowMessageNotificationAddUserFragment : MeeraPushNotificationSettingsAction
    data object ShowMessageNotificationUserFragment : MeeraPushNotificationSettingsAction
    data object ShowSubscriptionsNotificationAddUsersFragment : MeeraPushNotificationSettingsAction
    data object ShowSubscriptionsNotificationUsersFragment : MeeraPushNotificationSettingsAction
    data class UpdateOtherSetting(val listSettings: List<PushSettingsData>): MeeraPushNotificationSettingsAction

    sealed interface Message {
        data object NewMessage : MeeraPushNotificationSettingsAction
        data object ShowTextMessage : MeeraPushNotificationSettingsAction
        class ExceptUser(val usersCount: Int = 0) : MeeraPushNotificationSettingsAction
    }

    sealed interface Request {
        data object NewDialog : MeeraPushNotificationSettingsAction
        data object Calls : MeeraPushNotificationSettingsAction
        data object AddToFriend : MeeraPushNotificationSettingsAction
        data object AddToCommunity : MeeraPushNotificationSettingsAction
    }

    sealed interface Feedback {
        data object CommunityComments : MeeraPushNotificationSettingsAction
        data object PostComments : MeeraPushNotificationSettingsAction
        data object MomentComments : MeeraPushNotificationSettingsAction
        data object PostCommentAnswer : MeeraPushNotificationSettingsAction
        data object MomentCommentAnswer : MeeraPushNotificationSettingsAction
        data object ShowCommentsText : MeeraPushNotificationSettingsAction
        data object PostReaction : MeeraPushNotificationSettingsAction
        data object MomentReaction : MeeraPushNotificationSettingsAction
        data object PostCommentReaction : MeeraPushNotificationSettingsAction
        data object MomentCommentReaction : MeeraPushNotificationSettingsAction
        data object AboutMePhotoReaction : MeeraPushNotificationSettingsAction
    }

    sealed interface ProfileEvents {
        data object ReceiveNewGift : MeeraPushNotificationSettingsAction
        data object FriendsBirthday : MeeraPushNotificationSettingsAction
    }

    sealed interface Mentions {
        data object GroupChatMention : MeeraPushNotificationSettingsAction
        data object PostMention : MeeraPushNotificationSettingsAction
        data object CommentMention : MeeraPushNotificationSettingsAction
    }

    sealed interface Post {
        class FriendAndSubscriptions(val usersCount: Int = 0) : MeeraPushNotificationSettingsAction
    }
}
