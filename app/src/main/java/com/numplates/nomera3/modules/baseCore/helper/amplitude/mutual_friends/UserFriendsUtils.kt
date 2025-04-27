package com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_FRIENDS
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_MUTUAL_USERS
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_SUBSCRIBERS
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_SUBSCRIPTIONS

fun Int.toScreenAddFriendAmplitude(): FriendAddAction? {
    return when (this) {
        MODE_SHOW_USER_FRIENDS -> FriendAddAction.USER_FRIENDS
        MODE_SHOW_USER_SUBSCRIBERS -> FriendAddAction.USER_FOLLOWERS
        MODE_SHOW_USER_SUBSCRIPTIONS -> FriendAddAction.USER_FOLLOWS
        MODE_SHOW_USER_MUTUAL_USERS -> FriendAddAction.COMMON_FOLLOWS
        else -> null
    }
}

fun Int.toScreenFollowActionAmplitude(): AmplitudeFollowButtonPropertyWhere? {
    return when (this) {
        MODE_SHOW_USER_FRIENDS -> AmplitudeFollowButtonPropertyWhere.USER_FRIENDS
        MODE_SHOW_USER_SUBSCRIBERS -> AmplitudeFollowButtonPropertyWhere.USER_FOLLOWERS
        MODE_SHOW_USER_SUBSCRIPTIONS -> AmplitudeFollowButtonPropertyWhere.USER_FOLLOWS
        MODE_SHOW_USER_MUTUAL_USERS -> AmplitudeFollowButtonPropertyWhere.MUTUAL_FOLLOWS
        else -> null
    }
}

fun Int.toAmplitudePropertyWhere(): AmplitudePropertyWhere {
    return when (this) {
        MODE_SHOW_USER_FRIENDS -> AmplitudePropertyWhere.USER_FRIENDS
        MODE_SHOW_USER_SUBSCRIBERS -> AmplitudePropertyWhere.USER_FOLLOWERS
        MODE_SHOW_USER_SUBSCRIPTIONS -> AmplitudePropertyWhere.USER_FOLLOWS
        MODE_SHOW_USER_MUTUAL_USERS -> AmplitudePropertyWhere.MUTUAL_FOLLOWS
        else -> AmplitudePropertyWhere.OTHER
    }
}
