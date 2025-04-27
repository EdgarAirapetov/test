package com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton

import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment

fun DestinationOriginEnum?.toAmplitudeFollowButtonPropertyWhere(): AmplitudeFollowButtonPropertyWhere {
    return when (this) {
        DestinationOriginEnum.MAIN_ROAD -> AmplitudeFollowButtonPropertyWhere.MAIN_FEED
        DestinationOriginEnum.CUSTOM_ROAD -> AmplitudeFollowButtonPropertyWhere.SELF_FEED
        DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> AmplitudeFollowButtonPropertyWhere.FOLLOW_FEED
        DestinationOriginEnum.OWN_PROFILE -> AmplitudeFollowButtonPropertyWhere.PROFILE_FEED
        DestinationOriginEnum.OTHER_PROFILE -> AmplitudeFollowButtonPropertyWhere.USER_PROFILE_FEED
        DestinationOriginEnum.CHAT -> AmplitudeFollowButtonPropertyWhere.CHAT
        DestinationOriginEnum.HASHTAG -> AmplitudeFollowButtonPropertyWhere.HASHTAG
        DestinationOriginEnum.COMMUNITY -> AmplitudeFollowButtonPropertyWhere.COMMUNITY
        DestinationOriginEnum.NOTIFICATIONS,
        DestinationOriginEnum.NOTIFICATIONS_REACTIONS -> AmplitudeFollowButtonPropertyWhere.NOTIFICATION
        else -> AmplitudeFollowButtonPropertyWhere.OTHER
    }
}

fun BaseRoadsFragment.RoadTypeEnum.toAmplitudeFollowButtonPropertyWhere(): AmplitudeFollowButtonPropertyWhere {
    return when (this) {
        BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> AmplitudeFollowButtonPropertyWhere.MAIN_FEED
        BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD -> AmplitudeFollowButtonPropertyWhere.OTHER
        BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> AmplitudeFollowButtonPropertyWhere.FOLLOW_FEED
    }
}

fun RoadTypesEnum.toAmplitudeFollowButtonPropertyWhere(): AmplitudeFollowButtonPropertyWhere {
    return when (this) {
        RoadTypesEnum.MAIN -> AmplitudeFollowButtonPropertyWhere.MAIN_FEED
        RoadTypesEnum.HASHTAG -> AmplitudeFollowButtonPropertyWhere.HASHTAG
        RoadTypesEnum.SUBSCRIPTION -> AmplitudeFollowButtonPropertyWhere.FOLLOW_FEED
        RoadTypesEnum.COMMUNITY -> AmplitudeFollowButtonPropertyWhere.COMMUNITY
        RoadTypesEnum.CUSTOM,
        RoadTypesEnum.PERSONAL -> AmplitudeFollowButtonPropertyWhere.OTHER
    }
}
