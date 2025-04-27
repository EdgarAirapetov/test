package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSettingsFlags
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_USER
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSettingsFlagsModel
import com.numplates.nomera3.modules.userprofile.ui.model.SettingsFlags

fun UserSettingsFlags.toFriendRelationshipAmplitude(): FriendRelationshipProperty {
    return when {
        this.friendStatus == REQUEST_NOT_CONFIRMED_BY_USER -> FriendRelationshipProperty.FOLLOW
        this.friendStatus == FRIEND_STATUS_CONFIRMED -> FriendRelationshipProperty.FRIEND
        this.subscription_on.toBoolean() -> FriendRelationshipProperty.MUTUAL_FOLLOW
        this.subscribedToMe.toBoolean()
            || this.friendStatus == REQUEST_NOT_CONFIRMED_BY_ME -> FriendRelationshipProperty.FOLLOWER
        else -> FriendRelationshipProperty.NOBODY
    }
}

fun SettingsFlags.toFriendRelationshipAmplitude(): FriendRelationshipProperty {
    return when {
        this.friendStatus == REQUEST_NOT_CONFIRMED_BY_USER -> FriendRelationshipProperty.FOLLOW
        this.friendStatus == FRIEND_STATUS_CONFIRMED -> FriendRelationshipProperty.FRIEND
        this.isSubscriptionOn -> FriendRelationshipProperty.MUTUAL_FOLLOW
        this.isSubscribedToMe
            || this.friendStatus == REQUEST_NOT_CONFIRMED_BY_ME -> FriendRelationshipProperty.FOLLOWER
        else -> FriendRelationshipProperty.NOBODY
    }
}

fun UserSettingsFlagsModel.toFriendRelationshipAmplitude(): FriendRelationshipProperty {
    return when {
        this.friendStatus == REQUEST_NOT_CONFIRMED_BY_USER -> FriendRelationshipProperty.FOLLOW
        this.friendStatus == FRIEND_STATUS_CONFIRMED -> FriendRelationshipProperty.FRIEND
        this.subscription_on.toBoolean() -> FriendRelationshipProperty.MUTUAL_FOLLOW
        this.subscribedToMe.toBoolean()
            || this.friendStatus == REQUEST_NOT_CONFIRMED_BY_ME -> FriendRelationshipProperty.FOLLOWER
        else -> FriendRelationshipProperty.NOBODY
    }
}

fun UserPost.toFriendRelationshipAmplitude(): FriendRelationshipProperty {
    return when {
        this.friendStatus == REQUEST_NOT_CONFIRMED_BY_USER -> FriendRelationshipProperty.FOLLOW
        this.friendStatus == FRIEND_STATUS_CONFIRMED -> FriendRelationshipProperty.FRIEND
        this.subscriptionOn.toBoolean() -> FriendRelationshipProperty.MUTUAL_FOLLOW
        this.subscribedToMe.toBoolean()
            || this.friendStatus == REQUEST_NOT_CONFIRMED_BY_ME -> FriendRelationshipProperty.FOLLOWER
        else -> FriendRelationshipProperty.NOBODY
    }
}

fun createInfluencerAmplitudeProperty(
    topContentMaker: Boolean,
    approved: Boolean
): AmplitudeInfluencerProperty {
    return when {
        approved -> AmplitudeInfluencerProperty.INFLUENCER
        topContentMaker -> AmplitudeInfluencerProperty.HOT_AUTHOR
        else -> AmplitudeInfluencerProperty.NONE
    }
}
