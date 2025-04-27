package com.numplates.nomera3.modules.search.ui.entity.event

import androidx.annotation.StringRes
import com.numplates.nomera3.R

sealed class SearchMessageViewEvent(@StringRes val message: Int?) {
    object GroupSubscribed : SearchMessageViewEvent(R.string.group_joined)
    object GroupSendRequest : SearchMessageViewEvent(R.string.request_send)

    object UserUnsubscribed : SearchMessageViewEvent(null)
    object UserSubscribed : SearchMessageViewEvent(R.string.subscribed_on_user_notif_on)
    object UserAddToFriendWhileNoSubscribed :
        SearchMessageViewEvent(R.string.friend_request_send_notiff_on)

    object UserAddToFriendWhileSubscribed : SearchMessageViewEvent(R.string.request_sent)
    object UserDeclinedIncomingFriendRequest : SearchMessageViewEvent(R.string.request_rejected)
    object UserAcceptedIncomingFriendRequest : SearchMessageViewEvent(null)

    object Error : SearchMessageViewEvent(R.string.error_try_later)

    data class ClearRecentMessage(val delaySec: Int) :
        SearchMessageViewEvent(R.string.meera_search_recent_list_clear_timer)
}
