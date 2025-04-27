package com.numplates.nomera3.modules.feed.ui.entity

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.MeeraBaseRoadsFragment

enum class DestinationOriginEnum {
    MAIN_ROAD,
    CUSTOM_ROAD,
    SUBSCRIPTIONS_ROAD,
    OWN_PROFILE,
    OTHER_PROFILE,
    CHAT,
    HASHTAG,
    COMMUNITY,
    NOTIFICATIONS,
    NOTIFICATIONS_REACTIONS,
    DEEPLINK,
    PUSH,
    ANNOUNCEMENT;

    companion object {

        fun fromRoadType(roadType: BaseRoadsFragment.RoadTypeEnum): DestinationOriginEnum {
            return when (roadType) {
                BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> MAIN_ROAD
                BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD -> CUSTOM_ROAD
                BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> SUBSCRIPTIONS_ROAD
            }
        }

        fun fromRoadType(roadType: MeeraBaseRoadsFragment.RoadTypeEnum): DestinationOriginEnum {
            return when (roadType) {
                MeeraBaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> MAIN_ROAD
                MeeraBaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD -> CUSTOM_ROAD
                MeeraBaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> SUBSCRIPTIONS_ROAD
            }
        }

        fun fromNetworkRoadType(networkRoadType: NetworkRoadType): DestinationOriginEnum {
            return when (networkRoadType) {
                is NetworkRoadType.COMMUNITY -> COMMUNITY
                NetworkRoadType.HASHTAG -> HASHTAG
                NetworkRoadType.SUBSCRIPTIONS -> SUBSCRIPTIONS_ROAD
                is NetworkRoadType.USER -> {
                    if (networkRoadType.isMe) {
                        OWN_PROFILE
                    } else {
                        OTHER_PROFILE
                    }
                }
                NetworkRoadType.ALL -> MAIN_ROAD
            }
        }

        fun fromDeeplinkOrigin(deeplinkOrigin: DeeplinkOrigin): DestinationOriginEnum {
            return when (deeplinkOrigin) {
                DeeplinkOrigin.APP_VIEW -> DEEPLINK
                DeeplinkOrigin.NOTIFICATIONS -> NOTIFICATIONS
                DeeplinkOrigin.PUSH -> PUSH
                DeeplinkOrigin.ANNOUNCEMENT -> ANNOUNCEMENT
            }
        }
    }
}

fun DestinationOriginEnum?.toAmplitudePropertyWhere(): AmplitudePropertyWhere {
    return when (this) {
        DestinationOriginEnum.MAIN_ROAD -> AmplitudePropertyWhere.MAIN_FEED
        DestinationOriginEnum.CUSTOM_ROAD -> AmplitudePropertyWhere.SELF_FEED
        DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> AmplitudePropertyWhere.FOLLOW_FEED
        DestinationOriginEnum.OWN_PROFILE -> AmplitudePropertyWhere.PROFILE
        DestinationOriginEnum.OTHER_PROFILE -> AmplitudePropertyWhere.USER_PROFILE
        DestinationOriginEnum.CHAT -> AmplitudePropertyWhere.CHAT
        DestinationOriginEnum.HASHTAG -> AmplitudePropertyWhere.HASHTAG
        DestinationOriginEnum.COMMUNITY -> AmplitudePropertyWhere.COMMUNITY
        DestinationOriginEnum.NOTIFICATIONS, DestinationOriginEnum.NOTIFICATIONS_REACTIONS ->
            AmplitudePropertyWhere.NOTIFICATIONS
        DestinationOriginEnum.DEEPLINK -> AmplitudePropertyWhere.DEEPLINK
        DestinationOriginEnum.PUSH -> AmplitudePropertyWhere.PUSH
        DestinationOriginEnum.ANNOUNCEMENT -> AmplitudePropertyWhere.ANNOUNCEMENT
        else -> AmplitudePropertyWhere.OTHER
    }
}

fun DestinationOriginEnum?.toAmplitudePropertyWhence(): AmplitudePropertyWhence {
    return when (this) {
        DestinationOriginEnum.MAIN_ROAD -> AmplitudePropertyWhence.MAIN_FEED
        DestinationOriginEnum.CUSTOM_ROAD -> AmplitudePropertyWhence.SELF_FEED
        DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> AmplitudePropertyWhence.FOLLOW_FEED
        DestinationOriginEnum.OWN_PROFILE -> AmplitudePropertyWhence.PROFILE
        DestinationOriginEnum.OTHER_PROFILE -> AmplitudePropertyWhence.USER_PROFILE
        DestinationOriginEnum.CHAT -> AmplitudePropertyWhence.CHAT
        DestinationOriginEnum.HASHTAG -> AmplitudePropertyWhence.HASHTAG
        DestinationOriginEnum.COMMUNITY -> AmplitudePropertyWhence.COMMUNITY
        DestinationOriginEnum.NOTIFICATIONS,
        DestinationOriginEnum.NOTIFICATIONS_REACTIONS,
        DestinationOriginEnum.PUSH -> AmplitudePropertyWhence.NOTIFICATION
        else -> AmplitudePropertyWhence.OTHER
    }
}
